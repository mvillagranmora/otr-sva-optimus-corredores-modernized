SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::NCG380Cus";

 
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::NCG380Cus"
( 
    -- Parametros de Entrada
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100))
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	 		  "BSCL.Normativa.BD::NOR.SALIDA_NCG380CUS" --Se debe llamar siemte p_out_TablaSalida
   
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER  
	DEFAULT SCHEMA "BSCL" 
AS 
  BEGIN
  
--DECLARACIÓN VARIABLES

  	declare v_ParametrosSolicitudFini DATE DEFAULT '0001-01-01';
	declare v_ParametrosSolicitudFter DATE DEFAULT '2050-01-01';
	declare v_Secuencia int;
	DECLARE v_IdDocumento,v_IdEmpresa, v_Count INT:=0;
	
	declare v_TRut		 int DEFAULT 0;	
	declare v_TNemo		 int DEFAULT 0;	
	
	declare v_Mercado		 NVARCHAR(10) DEFAULT '0';	
	
		
	declare v_CondFiltrosRut 	 int DEFAULT 0;
	declare v_CondFiltrosNemo	 int DEFAULT 0;
	declare v_CondFinal			 int DEFAULT 0;
	declare v_CondFecha          int DEFAULT 0;
	declare v_cond_TRut          int DEFAULT 0;
	declare v_cond_TNemo         int DEFAULT 0;
	declare v_cond_TMcdo         int DEFAULT 0;
	
	
	
	

	DECLARE v_ParametrosSolicitudRut    TABLE("TipoParametro" NVARCHAR(100),"Valor" NVARCHAR(100));
    DECLARE v_ParametrosSolicitudNemo   TABLE("TipoParametro" NVARCHAR(100),"Valor" NVARCHAR(100)); 

	
	declare v_out_salidaspcus	TABLE   
		(
		"FolioCartera"						BIGINT
		,"IdCartera" 						BIGINT
		--,"IdPortafolio" 					BIGINT
		,"NumeroCliente"				    BIGINT 
    	,"NumeroCuenta"				    	BIGINT 
    	,"Fecha" 							DATE 
    	,"Hora" 							NVARCHAR(10) 
    	,"NumeroComprobante"				BIGINT	
    	,"Emisor" 							NVARCHAR(50)
    	,"TipoMovimiento" 					NVARCHAR(10)
    	,"Movimiento" 						BIGINT
		,"Isin"								NVARCHAR(50)
		,"Nemotecnico" 						NVARCHAR(40)
		,"Cantidad" 						DECIMAL(26,8)
		,"Monto"	 						DECIMAL(26,8)
		,"Moneda"        					NVARCHAR(3) 
		,"Saldo" 							DECIMAL(26,8)
		,"SubCustodio" 						NVARCHAR(3)
		,"Estado"							NVARCHAR(40)
		,"DocumentoIdentidad"               NVARCHAR(40)
		,"IdTipoOperacion"                  NVARCHAR(10)
		,"IdClasificacionTipoInstrumento"   NVARCHAR(8)

		);


--------------------
--LOGICA
--------------------

--select top 1 "IdSolicitudCircular" into p_IdSolicitudCircular from "BSCL.Normativa.BD::NOR.SolicitudCircular" where "IdDocumento" = p_IdDocumento;
--Valores tabla parametros

SELECT  COUNT(1) INTO v_Count FROM :p_TablaParametros;			
if v_Count>0 THEN
	SELECT TOP 1  "Valor" INTO v_IdEmpresa
		FROM :p_TablaParametros SOL WHERE SOL."TipoParametro"='IdEmpresa';
		
	v_Datos = SELECT 
		  CASE WHEN SOL."TipoParametro"='fechaDesde'	THEN "Valor" ELSE NULL END AS "FINI"
		, CASE WHEN SOL."TipoParametro"='fechaHasta'	THEN "Valor" ELSE NULL END AS "FTER"
		FROM :p_TablaParametros SOL;
END IF;

v_ParametrosSolicitudRut = select '' as "TipoParametro", '' as "Valor" from dummy where 1=2;
v_ParametrosSolicitudNemo = select '' as "TipoParametro", '' as "Valor" from dummy where 1=2;

SELECT TOP 1 "IdEmpresa" INTO v_IdEmpresa 
		FROM "BSCL.Normativa.BD::DOC.Documento"
		WHERE "IdTipoDocumento" = 'NCG380CUS' order by "IdDocumento" desc;

--v_CustodiaFiltradosFecha = select 0 as "IdCartera" ,0 as "IdCliente", '' as "DocumentoIdentidadRut", '' as "FechaLiquidacion" from dummy where 1=2;

--rescato valor filtro fecha inicio

select top 1 "Valor" into v_ParametrosSolicitudFini from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'FINI';

--pregunto si existe fecha de termino ingresada desde la UI

select count(1) into v_CondFecha from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" 
where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular 
and "SP"."TipoParametro" = 'FTER';

IF(v_CondFecha >0) THEN

--si existe, la guardo en variable
select top 1 "Valor" into v_ParametrosSolicitudFter from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'FTER';
END IF;

--select "Valor","TipoParametro" from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = 2187 


--rescato valores de checkbox todos clientes
select count(1) into v_cond_TRut from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'TRUT';

IF (v_cond_TRut>0)
THEN
--rescato valor checkbox todos clientes
select "SP"."Valor" into v_TRut   from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'TRUT';
END IF;

--pregunto si checkbox todos nemo , posee información
select count(1) into v_cond_TNemo from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'TNEMO';

IF (v_cond_TNemo>0)
THEN
--guardo valor checkbox todos nemo
select "SP"."Valor" into v_TNemo  from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'TNEMO';
END IF;

--pregunto si checkbox todos mcdo , posee información
select count(1) into v_cond_TMcdo from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'MCDO';

IF (v_cond_TMcdo>0)
THEN
--guardo valor checkbox todos mcdo
select top 1 "Valor" into v_Mercado from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'MCDO';
END IF;

 
--reviso si existen valores ingredos (de a 1 o listado) de Clientes y/o Nemos
select count(1) into v_CondFiltrosRut from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" 
where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular 
and "SP"."TipoParametro" = 'RUT';

select count(1) into v_CondFiltrosNemo from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" 
where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular 
and "SP"."TipoParametro" = 'NEMO';


--sólo rescato filtro por rut si es que no se selecciono botón "todos"
-- si checkbox todos cli es false
IF v_TRut = 0 THEN
	--y existen valores rut ingresados
		IF v_CondFiltrosRut > 0 THEN 
		--almaceno todos los rut ingresados desde la UI
		v_ParametrosSolicitudRut = 
		select "SP"."TipoParametro" as "TipoParametro", 
		"SP"."Valor" as "Valor" 
		--(ltrim(rtrim(substring("SP"."Valor",1,LENGTH("SP"."Valor")-2)))) as "Valor" 
		from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" 
		where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular 
		and "SP"."TipoParametro" = 'RUT';
		
		END IF;	
END IF;	



--sólo rescato filtro por Nemo si es que no se selecciono botón "todos"
-- si checkbox todos nemo es false
IF v_TNemo = 0 THEN
		--y existen valores nemo ingresados
		IF v_CondFiltrosNemo > 0 THEN 
		--almaceno todos los nemos ingresados desde la UI
		v_ParametrosSolicitudNemo = 
		select "SP"."TipoParametro" as "TipoParametro", 
		"SP"."Valor" as "Valor" 
		from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" 
		where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular 
		and "SP"."TipoParametro" = 'NEMO';
		
		END IF;	
END IF;	

	--inicializo tabla salidaspcus (almacenará datos entregados por el servicio ConsultarCartolaCustodia)
		v_out_salidaspcus = 
		select
		0 as "FolioCartera"	
		,0 as "IdCartera" 						
	--	,0 as "IdPortafolio" 					
		,0 as "NumeroCliente"				     
    	,0 as "NumeroCuenta"				    	 
    	,TO_DATE('01-01-1900','DD-MM-YYYY') as "Fecha" 						 
    	,'' as "Hora" 							
    	,0 as "NumeroComprobante"				
    	,'' as "Emisor" 							
    	,'' as "TipoMovimiento" 					
    	,0 as "Movimiento" 						
		,'' as "Isin"								
		,'' as "Nemotecnico" 						
		,0 as "Cantidad" 						
		,0 as "Monto"	 					
		,'' as "Moneda"        					
		,0 as "Saldo" 							
		,'' as "SubCustodio" 						
		,'' as "Estado"							
		,'' as "DocumentoIdentidad" 
		,'' as "IdTipoOperacion"
		,'' as "IdClasificacionTipoInstrumento"

		from dummy where 1=2;





--se invoca servicio ConsultarCartolaCustodia, se envian filtros fecha ini y fecha hasta y el resultado se almacena en tabla v_out_salidaspcus
call "BSCL.Normativa.BD.Servicios.NCG380::ConsultarCartolaCustodia"(p_IdEmpresa=>:v_IdEmpresa,p_FechaInicio=>:v_ParametrosSolicitudFini,p_FechaTermino=>:v_ParametrosSolicitudFter,p_out_TablaSalida=>:v_out_salidaspcus);

--select * from :v_out_salidaspcus;

/*
select v_TRut as todosrut, v_TNemo as todosnemo,
v_CondFiltrosRut as ruts,v_CondFiltrosNemo as nemos,v_Mercado as mcdo
from dummy;
*/

--primera condición, botones "todos" sin marcar
IF (v_TRut = 0) AND  (v_TNemo = 0) THEN 
		
		--condicion 1.1 , ambos filtros ingresados
		IF (v_CondFiltrosRut > 0) AND  (v_CondFiltrosNemo > 0) THEN 
		
		p_out_TablaSalida=
		select 
		"SC"."DocumentoIdentidad" as "NumeroCliente"
		,"SC"."NumeroCuenta"	as "NumeroCuenta"
		,"SC"."Fecha" as "Fecha"
		,"SC"."Hora" as "Hora"
		,"SC"."NumeroComprobante" as "NumeroComprobante"
		,"SC"."Emisor"  as "Emisor"
		,IFNULL((select "HOM"."ValorDestino" from "BSCL.Normativa.BD::DOC.Homologacion" as "HOM" where "HOM"."ValorOrigen" = "SC"."IdTipoOperacion" and "HOM"."IdEstado" = "SC"."TipoMovimiento" ),'') as "TipoMovimiento"
	--	,case when ("TipoMovimiento" = 'INGRESO' OR "TipoMovimiento" = 'RETIROG')  then 'ICC'
	--	when ("TipoMovimiento" = 'RETIRO' OR "TipoMovimiento" = 'INGRESOG') then 'ECV'
	--	else '-' end as "TipoMovimiento"
		,"SC"."Movimiento" as "Movimiento"
		,"SC"."Isin"	 as "ISIN"
		,"SC"."Nemotecnico" as "Nemotecnico"
		,"SC"."Cantidad" as "Cantidad" 
		,"SC"."Monto" as "Monto"
		,"SC"."Moneda" as "Moneda"
		,"SC"."Saldo" as "Saldo"
		,"SC"."SubCustodio" as "SubCustodio"
		,"SC"."Estado" as "Estado"
 
		from 
		:v_out_salidaspcus as "SC"
		where "SC"."Nemotecnico" in (select distinct "PSN"."Valor" from :v_ParametrosSolicitudNemo as "PSN")
		and (ltrim(rtrim(substring("SC"."DocumentoIdentidad",1,LENGTH("SC"."DocumentoIdentidad")-2)))) in (select distinct "PSR"."Valor" from :v_ParametrosSolicitudRut as "PSR") 		
		and "SC"."IdClasificacionTipoInstrumento" = case when v_Mercado  <> '0' then  v_Mercado else  "SC"."IdClasificacionTipoInstrumento" end
		;
		
		
		END IF;	
		
		
		--condicion 1.2 , sólo filtros nemo 
		IF (v_CondFiltrosNemo > 0) AND (v_CondFiltrosRut = 0)  THEN 
		
		
		p_out_TablaSalida=
		select 
		"SC"."DocumentoIdentidad" as "NumeroCliente"
		,"SC"."NumeroCuenta"	as "NumeroCuenta"
		,"SC"."Fecha" as "Fecha"
		,"SC"."Hora" as "Hora"
		,"SC"."NumeroComprobante" as "NumeroComprobante"
		,"SC"."Emisor"  as "Emisor"
		,IFNULL((select "HOM"."ValorDestino" from "BSCL.Normativa.BD::DOC.Homologacion" as "HOM" where "HOM"."ValorOrigen" = "SC"."IdTipoOperacion" and "HOM"."IdEstado" = "SC"."TipoMovimiento" ),'') as "TipoMovimiento"
	--	,case when ("TipoMovimiento" = 'INGRESO' OR "TipoMovimiento" = 'RETIROG')  then 'ICC'
	--	when ("TipoMovimiento" = 'RETIRO' OR "TipoMovimiento" = 'INGRESOG') then 'ECV'
	--	else '-' end as "TipoMovimiento"
		,"SC"."Movimiento" as "Movimiento"
		,"SC"."Isin"	 as "ISIN"
		,"SC"."Nemotecnico" as "Nemotecnico"
		,"SC"."Cantidad" as "Cantidad" 
		,"SC"."Monto" as "Monto"
		,"SC"."Moneda" as "Moneda"
		,"SC"."Saldo" as "Saldo"
		,"SC"."SubCustodio" as "SubCustodio"
		,"SC"."Estado" as "Estado"
		
		from 
		:v_out_salidaspcus as "SC"
		where "SC"."Nemotecnico" in (select distinct "PSN"."Valor" from :v_ParametrosSolicitudNemo as "PSN")
		and "SC"."IdClasificacionTipoInstrumento" = case when v_Mercado  <> '0' then  v_Mercado else  "SC"."IdClasificacionTipoInstrumento" end
		;		
		
		
		END IF;	
		
		--condicion 1.3 , sólo filtros rut 
		IF (v_CondFiltrosRut > 0) AND  (v_CondFiltrosNemo = 0) THEN 
		
		
		p_out_TablaSalida=
		select 
		"SC"."DocumentoIdentidad" as "NumeroCliente"
		,"SC"."NumeroCuenta"	as "NumeroCuenta"
		,"SC"."Fecha" as "Fecha"
		,"SC"."Hora" as "Hora"
		,"SC"."NumeroComprobante" as "NumeroComprobante"
		,"SC"."Emisor"  as "Emisor"
		,IFNULL((select "HOM"."ValorDestino" from "BSCL.Normativa.BD::DOC.Homologacion" as "HOM" where "HOM"."ValorOrigen" = "SC"."IdTipoOperacion" and "HOM"."IdEstado" = "SC"."TipoMovimiento" ),'') as "TipoMovimiento"
	--	,case when ("TipoMovimiento" = 'INGRESO' OR "TipoMovimiento" = 'RETIROG')  then 'ICC'
	--	when ("TipoMovimiento" = 'RETIRO' OR "TipoMovimiento" = 'INGRESOG') then 'ECV'
	--	else '-' end as "TipoMovimiento"
		,"SC"."Movimiento" as "Movimiento"
		,"SC"."Isin"	 as "ISIN"
		,"SC"."Nemotecnico" as "Nemotecnico"
		,"SC"."Cantidad" as "Cantidad" 
		,"SC"."Monto" as "Monto"
		,"SC"."Moneda" as "Moneda"
		,"SC"."Saldo" as "Saldo"
		,"SC"."SubCustodio" as "SubCustodio"
		,"SC"."Estado" as "Estado"
	
		from 
		:v_out_salidaspcus as "SC"
		where (ltrim(rtrim(substring("SC"."DocumentoIdentidad",1,LENGTH("SC"."DocumentoIdentidad")-2)))) in (select distinct "PSR"."Valor" from :v_ParametrosSolicitudRut as "PSR") 		
		and "SC"."IdClasificacionTipoInstrumento" = case when v_Mercado  <> '0' then  v_Mercado else  "SC"."IdClasificacionTipoInstrumento" end
		;
		
		END IF;	
		
		
		--condicion 1.4 , sin filtros ingresados
		IF (v_CondFiltrosRut = 0) AND  (v_CondFiltrosNemo = 0) THEN 
		
		
		p_out_TablaSalida=
		select 
		"SC"."DocumentoIdentidad" as "NumeroCliente"
		,"SC"."NumeroCuenta"	as "NumeroCuenta"
		,"SC"."Fecha" as "Fecha"
		,"SC"."Hora" as "Hora"
		,"SC"."NumeroComprobante" as "NumeroComprobante"
		,"SC"."Emisor"  as "Emisor"
		,IFNULL((select "HOM"."ValorDestino" from "BSCL.Normativa.BD::DOC.Homologacion" as "HOM" where "HOM"."ValorOrigen" = "SC"."IdTipoOperacion" and "HOM"."IdEstado" = "SC"."TipoMovimiento" ),'') as "TipoMovimiento"
	--	,case when ("TipoMovimiento" = 'INGRESO' OR "TipoMovimiento" = 'RETIROG')  then 'ICC'
	--	when ("TipoMovimiento" = 'RETIRO' OR "TipoMovimiento" = 'INGRESOG') then 'ECV'
	--	else '-' end as "TipoMovimiento"
		,"SC"."Movimiento" as "Movimiento"
		,"SC"."Isin"	 as "ISIN"
		,"SC"."Nemotecnico" as "Nemotecnico"
		,"SC"."Cantidad" as "Cantidad" 
		,"SC"."Monto" as "Monto"
		,"SC"."Moneda" as "Moneda"
		,"SC"."Saldo" as "Saldo"
		,"SC"."SubCustodio" as "SubCustodio"
		,"SC"."Estado" as "Estado"
		from 
		:v_out_salidaspcus as "SC"
		where "SC"."IdClasificacionTipoInstrumento" = case when v_Mercado  <> '0' then  v_Mercado else  "SC"."IdClasificacionTipoInstrumento" end
		
		;

		END IF;	
		
END IF;	

--segunda condición, botón todos rut marcado
IF (v_TRut = 1) AND  (v_TNemo = 0) THEN 

		--condicion 2.1 , filtro nemo ingresado
		IF (v_CondFiltrosNemo > 0) THEN 
		
		
		p_out_TablaSalida=
		select 
		"SC"."DocumentoIdentidad" as "NumeroCliente"
		,"SC"."NumeroCuenta"	as "NumeroCuenta"
		,"SC"."Fecha" as "Fecha"
		,"SC"."Hora" as "Hora"
		,"SC"."NumeroComprobante" as "NumeroComprobante"
		,"SC"."Emisor"  as "Emisor"
		,IFNULL((select "HOM"."ValorDestino" from "BSCL.Normativa.BD::DOC.Homologacion" as "HOM" where "HOM"."ValorOrigen" = "SC"."IdTipoOperacion" and "HOM"."IdEstado" = "SC"."TipoMovimiento" ),'') as "TipoMovimiento"
	--	,case when ("TipoMovimiento" = 'INGRESO' OR "TipoMovimiento" = 'RETIROG')  then 'ICC'
	--	when ("TipoMovimiento" = 'RETIRO' OR "TipoMovimiento" = 'INGRESOG') then 'ECV'
	--	else '-' end as "TipoMovimiento"
		,"SC"."Movimiento" as "Movimiento"
		,"SC"."Isin"	 as "ISIN"
		,"SC"."Nemotecnico" as "Nemotecnico"
		,"SC"."Cantidad" as "Cantidad" 
		,"SC"."Monto" as "Monto"
		,"SC"."Moneda" as "Moneda"
		,"SC"."Saldo" as "Saldo"
		,"SC"."SubCustodio" as "SubCustodio"
		,"SC"."Estado" as "Estado"
		
		from 
		:v_out_salidaspcus as "SC"
		where "SC"."Nemotecnico" in (select distinct "PSN"."Valor" from :v_ParametrosSolicitudNemo as "PSN")
		and "SC"."IdClasificacionTipoInstrumento" = case when v_Mercado  <> '0' then  v_Mercado else  "SC"."IdClasificacionTipoInstrumento" end
		;
			
		
		END IF;	
		
		--condicion 2.2 , filtro nemo no ingresado
		IF (v_CondFiltrosNemo = 0) THEN 
	
			
		p_out_TablaSalida=
		select 
		"SC"."DocumentoIdentidad" as "NumeroCliente"
		,"SC"."NumeroCuenta"	as "NumeroCuenta"
		,"SC"."Fecha" as "Fecha"
		,"SC"."Hora" as "Hora"
		,"SC"."NumeroComprobante" as "NumeroComprobante"
		,"SC"."Emisor"  as "Emisor"
		,IFNULL((select "HOM"."ValorDestino" from "BSCL.Normativa.BD::DOC.Homologacion" as "HOM" where "HOM"."ValorOrigen" = "SC"."IdTipoOperacion" and "HOM"."IdEstado" = "SC"."TipoMovimiento" ),'') as "TipoMovimiento"
	--	,case when ("TipoMovimiento" = 'INGRESO' OR "TipoMovimiento" = 'RETIROG')  then 'ICC'
	--	when ("TipoMovimiento" = 'RETIRO' OR "TipoMovimiento" = 'INGRESOG') then 'ECV'
	--	else '-' end as "TipoMovimiento"
		,"SC"."Movimiento" as "Movimiento"
		,"SC"."Isin"	 as "ISIN"
		,"SC"."Nemotecnico" as "Nemotecnico"
		,"SC"."Cantidad" as "Cantidad" 
		,"SC"."Monto" as "Monto"
		,"SC"."Moneda" as "Moneda"
		,"SC"."Saldo" as "Saldo"
		,"SC"."SubCustodio" as "SubCustodio"
		,"SC"."Estado" as "Estado"
		
		from 
		:v_out_salidaspcus as "SC"
		where  "SC"."IdClasificacionTipoInstrumento" = case when v_Mercado  <> '0' then  v_Mercado else  "SC"."IdClasificacionTipoInstrumento" end
		;
			 
		
		END IF;	

END IF;	

--tercera condición, botón todos nemo marcado
IF (v_TNemo = 1) AND (v_TRut = 0) THEN 
		
		--condicion 3.1 , filtro rut ingresado
		IF (v_CondFiltrosRut > 0) THEN 
		
		p_out_TablaSalida=
		select 
		"SC"."DocumentoIdentidad" as "NumeroCliente"
		,"SC"."NumeroCuenta"	as "NumeroCuenta"
		,"SC"."Fecha" as "Fecha"
		,"SC"."Hora" as "Hora"
		,"SC"."NumeroComprobante" as "NumeroComprobante"
		,"SC"."Emisor"  as "Emisor"
		,IFNULL((select "HOM"."ValorDestino" from "BSCL.Normativa.BD::DOC.Homologacion" as "HOM" where "HOM"."ValorOrigen" = "SC"."IdTipoOperacion" and "HOM"."IdEstado" = "SC"."TipoMovimiento" ),'') as "TipoMovimiento"
	--	,case when ("TipoMovimiento" = 'INGRESO' OR "TipoMovimiento" = 'RETIROG')  then 'ICC'
	--	when ("TipoMovimiento" = 'RETIRO' OR "TipoMovimiento" = 'INGRESOG') then 'ECV'
	--	else '-' end as "TipoMovimiento"
		,"SC"."Movimiento" as "Movimiento"
		,"SC"."Isin"	 as "ISIN"
		,"SC"."Nemotecnico" as "Nemotecnico"
		,"SC"."Cantidad" as "Cantidad" 
		,"SC"."Monto" as "Monto"
		,"SC"."Moneda" as "Moneda"
		,"SC"."Saldo" as "Saldo"
		,"SC"."SubCustodio" as "SubCustodio"
		,"SC"."Estado" as "Estado"
		
		from 
		:v_out_salidaspcus as "SC"
		where (ltrim(rtrim(substring("SC"."DocumentoIdentidad",1,LENGTH("SC"."DocumentoIdentidad")-2)))) in (select distinct "PSR"."Valor" from :v_ParametrosSolicitudRut as "PSR") 		
		and "SC"."IdClasificacionTipoInstrumento" = case when v_Mercado  <> '0' then  v_Mercado else  "SC"."IdClasificacionTipoInstrumento" end
		;
		--where (ltrim(rtrim(substring("CUS"."DocumentoIdentidad",1,LENGTH("CUS"."DocumentoIdentidad")-2)))) in (select distinct "PSR"."Valor" from :v_ParametrosSolicitudRut as "PSR") 		
			
		
		END IF;	
		
		--condicion 3.2, sin filtro rut
		IF (v_CondFiltrosRut = 0) THEN 
		
		p_out_TablaSalida=
		select 
		"SC"."DocumentoIdentidad" as "NumeroCliente"
		,"SC"."NumeroCuenta"	as "NumeroCuenta"
		,"SC"."Fecha" as "Fecha"
		,"SC"."Hora" as "Hora"
		,"SC"."NumeroComprobante" as "NumeroComprobante"
		,"SC"."Emisor"  as "Emisor"
		,IFNULL((select "HOM"."ValorDestino" from "BSCL.Normativa.BD::DOC.Homologacion" as "HOM" where "HOM"."ValorOrigen" = "SC"."IdTipoOperacion" and "HOM"."IdEstado" = "SC"."TipoMovimiento" ),'') as "TipoMovimiento"
	--	,case when ("TipoMovimiento" = 'INGRESO' OR "TipoMovimiento" = 'RETIROG')  then 'ICC'
	--	when ("TipoMovimiento" = 'RETIRO' OR "TipoMovimiento" = 'INGRESOG') then 'ECV'
	--	else '-' end as "TipoMovimiento"
		,"SC"."Movimiento" as "Movimiento"
		,"SC"."Isin"	 as "ISIN"
		,"SC"."Nemotecnico" as "Nemotecnico"
		,"SC"."Cantidad" as "Cantidad" 
		,"SC"."Monto" as "Monto"
		,"SC"."Moneda" as "Moneda"
		,"SC"."Saldo" as "Saldo"
		,"SC"."SubCustodio" as "SubCustodio"
		,"SC"."Estado" as "Estado"
		from 
		:v_out_salidaspcus as "SC"
		where  "SC"."IdClasificacionTipoInstrumento" = case when v_Mercado  <> '0' then  v_Mercado else  "SC"."IdClasificacionTipoInstrumento" end
		;
		
		END IF;	

END IF;	


--cuarta condición, botónes todos marcados
IF (v_TRut = 1) AND (v_TNemo = 1) THEN 
		
		p_out_TablaSalida=
		select 
		"SC"."DocumentoIdentidad" as "NumeroCliente"
		,"SC"."NumeroCuenta"	as "NumeroCuenta"
		,"SC"."Fecha" as "Fecha"
		,"SC"."Hora" as "Hora"
		,"SC"."NumeroComprobante" as "NumeroComprobante"
		,"SC"."Emisor"  as "Emisor"
		,IFNULL((select "HOM"."ValorDestino" from "BSCL.Normativa.BD::DOC.Homologacion" as "HOM" where "HOM"."ValorOrigen" = "SC"."IdTipoOperacion" and "HOM"."IdEstado" = "SC"."TipoMovimiento" ),'') as "TipoMovimiento"
	--	,case when ("TipoMovimiento" = 'INGRESO' OR "TipoMovimiento" = 'RETIROG')  then 'ICC'
	--	when ("TipoMovimiento" = 'RETIRO' OR "TipoMovimiento" = 'INGRESOG') then 'ECV'
	--	else '-' end as "TipoMovimiento"
		,"SC"."Movimiento" as "Movimiento"
		,"SC"."Isin"	 as "ISIN"
		,"SC"."Nemotecnico" as "Nemotecnico"
		,"SC"."Cantidad" as "Cantidad" 
		,"SC"."Monto" as "Monto"
		,"SC"."Moneda" as "Moneda"
		,"SC"."Saldo" as "Saldo" 
		,"SC"."SubCustodio" as "SubCustodio"
		,"SC"."Estado" as "Estado"
		from 
		:v_out_salidaspcus as "SC"
		where "SC"."IdClasificacionTipoInstrumento" = case when v_Mercado  <> '0' then  v_Mercado else  "SC"."IdClasificacionTipoInstrumento" end
		;
		
END IF;


--select * from :v_out_salidaspcus where "DocumentoIdentidad" = '333-6';

select count(1) into v_CondFinal from :p_out_TablaSalida;


--select v_CondFinal as "condicion_final",v_TRut as "TodosRut", v_TNemo as "TodosNemo",v_ParametrosSolicitudFini as "fecini",v_ParametrosSolicitudFter as "fecter"  from dummy;

IF (v_CondFinal > 0) THEN 

SELECT * FROM :p_out_TablaSalida;

END IF;	

--select * from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular ;
--select * from :v_out_salidaspcus;

END;




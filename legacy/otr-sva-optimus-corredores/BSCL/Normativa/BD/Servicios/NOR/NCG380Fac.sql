SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::NCG380Fac";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::NCG380Fac"
( 
    -- Parametros de Entrada
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100))
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	 		  "BSCL.Normativa.BD::NOR.SALIDA_NCG380FAC" --Se debe llamar siemte p_out_TablaSalida
    
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  


  	declare v_ParametrosSolicitudFini DATE DEFAULT '0001-01-01';
	declare v_ParametrosSolicitudFter DATE DEFAULT '2050-01-01';
	
	declare v_Secuencia int;
	DECLARE v_IdDocumento,v_IdEmpresa, v_Count INT:=0;
	
	declare v_TRut		 int DEFAULT 0;	
	declare v_TNemo		 int DEFAULT 0;
	declare v_CondMcdo	 int DEFAULT 0;	
	
	declare v_Mercado		 NVARCHAR(10) DEFAULT '0';	
		
	declare v_CondFiltrosRut 	 int DEFAULT 0;
	declare v_CondFiltrosNemo	 int DEFAULT 0;
	declare v_CondFinal			 int DEFAULT 0;
	declare v_CondFecha          int DEFAULT 0;
	declare v_CondTodos		 	 int DEFAULT 0;

	DECLARE v_FacturacionFiltradosFecha     TABLE("IdMovimientoComprobante" BIGINT,"DocumentoIdentidadRut" NVARCHAR(40), "FechaTransaccion" TIMESTAMP);
	
	DECLARE v_ParametrosSolicitudRut    TABLE("TipoParametro" NVARCHAR(100),"Valor" NVARCHAR(100));
    DECLARE v_ParametrosSolicitudNemo   TABLE("TipoParametro" NVARCHAR(100),"Valor" NVARCHAR(100)); 


--------------------
--LOGICA
--------------------

--select top 1 "IdSolicitudCircular" into p_IdSolicitudCircular from "BSCL.Normativa.BD::NOR.SolicitudCircular" where "IdDocumento" = p_IdDocumento;
--Valores tabla parametros

/*
SELECT  COUNT(1) INTO v_Count FROM :p_TablaParametros;			
if v_Count>0 THEN
	SELECT TOP 1  "Valor" INTO v_IdEmpresa
		FROM :p_TablaParametros SOL WHERE SOL."TipoParametro"='IdEmpresa';
		
	v_Datos = SELECT 
		  CASE WHEN SOL."TipoParametro"='fechaDesde'	THEN "Valor" ELSE NULL END AS "FINI"
		, CASE WHEN SOL."TipoParametro"='fechaHasta'	THEN "Valor" ELSE NULL END AS "FTER"
		FROM :p_TablaParametros SOL;
END IF;
*/



v_ParametrosSolicitudRut = select '' as "TipoParametro", '' as "Valor" from dummy where 1=2;
v_ParametrosSolicitudNemo = select '' as "TipoParametro", '' as "Valor" from dummy where 1=2;

v_FacturacionFiltradosFecha = select 0 as "IdMovimientoComprobante" , '' as "DocumentoIdentidadRut", '' as "FechaTransaccion" from dummy where 1=2;

SELECT TOP 1 "IdEmpresa" INTO v_IdEmpresa 
		FROM "BSCL.Normativa.BD::DOC.Documento"
		WHERE "IdTipoDocumento" = 'NCG380FAC' order by "IdDocumento" desc;


select top 1 "Valor" into v_ParametrosSolicitudFini from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'FINI';

select count(1) into v_CondFecha from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" 
where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular 
and "SP"."TipoParametro" = 'FTER';

IF(v_CondFecha >0) THEN
select top 1 "Valor" into v_ParametrosSolicitudFter from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'FTER';
END IF;

select count(1) into v_CondTodos   from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'TRUT';


--rescato valores de checkbox todos

IF(v_CondTodos>0)
THEN
select "SP"."Valor" into v_TRut   from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'TRUT';
END IF;


select 0 into v_CondTodos from dummy;


select count(1) into v_CondTodos   from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'TNEMO';

IF(v_CondTodos>0)
THEN
select "SP"."Valor" into v_TNemo  from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'TNEMO';
END IF;


select count(1) into v_CondMcdo from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'MCDO';


IF(v_CondMcdo>0)
THEN

select top 1 "Valor" into v_Mercado from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular and "SP"."TipoParametro" = 'MCDO';

END IF;




select count(1) into v_CondFiltrosRut from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" 
where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular 
and "SP"."TipoParametro" = 'RUT';

select count(1) into v_CondFiltrosNemo from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" 
where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular 
and "SP"."TipoParametro" = 'NEMO';


--sólo rescato filtro por rut si es que no se selecciono botón "todos"

IF v_TRut = 0 THEN
		IF v_CondFiltrosRut > 0 THEN 
		
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

IF v_TNemo = 0 THEN
		
		IF v_CondFiltrosNemo > 0 THEN 
		
		v_ParametrosSolicitudNemo = 
		select "SP"."TipoParametro" as "TipoParametro", 
		"SP"."Valor" as "Valor" 
		from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" 
		where "SP"."IdSolicitudCircular" = p_IdSolicitudCircular 
		and "SP"."TipoParametro" = 'NEMO';
		
		END IF;	
END IF;	

--primera condición, botones "todos" sin marcar
IF (v_TRut = 0) AND  (v_TNemo = 0) THEN 
		
		--condicion 1.1 , ambos filtros ingresados
		IF (v_CondFiltrosRut > 0) AND  (v_CondFiltrosNemo > 0) THEN 
		
		
		v_FacturacionFiltradosFecha = 
		select  
		"CMC"."IdMovimientoComprobante" as "IdMovimientoComprobante",
		"CMC"."DocumentoIdentidad" as "DocumentoIdentidadRut",
		"CMC"."FechaTransaccion" as "FechaTransaccion"
		from "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" as "CMC"
		where
		CAST("CMC"."FechaTransaccion" AS DATE)  >= v_ParametrosSolicitudFini
		and CAST("CMC"."FechaTransaccion" AS DATE)  <= IFNULL (v_ParametrosSolicitudFter,"CMC"."FechaTransaccion" )
		and "CMC"."NemotecnicoSubyacente"  in (select distinct "PSN"."Valor" from :v_ParametrosSolicitudNemo as "PSN")
		and (ltrim(rtrim(substring("CMC"."DocumentoIdentidad",1,LENGTH("CMC"."DocumentoIdentidad")-2)))) in (select distinct "PSR"."Valor" from :v_ParametrosSolicitudRut as "PSR" )
		and "CMC"."IdClasificacionTipoInstrumento" = case when v_Mercado  <> '0' then  v_Mercado else  "CMC"."IdClasificacionTipoInstrumento" end
		; 
		
		END IF;	
		
		
		--condicion 1.2 , sólo filtros nemo 
		IF (v_CondFiltrosNemo > 0) AND (v_CondFiltrosRut = 0)  THEN 
		
		
		v_FacturacionFiltradosFecha = 
		select  
		"CMC"."IdMovimientoComprobante" as "IdMovimientoComprobante",
		"CMC"."DocumentoIdentidad" as "DocumentoIdentidadRut",
		"CMC"."FechaTransaccion" as "FechaTransaccion"
		from "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" as "CMC"
		where
		CAST("CMC"."FechaTransaccion" AS DATE)  >= v_ParametrosSolicitudFini
		and CAST("CMC"."FechaTransaccion" AS DATE)  <= IFNULL (v_ParametrosSolicitudFter,"CMC"."FechaTransaccion" )
		and "CMC"."NemotecnicoSubyacente"  in (select distinct "PSN"."Valor" from :v_ParametrosSolicitudNemo as "PSN")
		and "CMC"."IdClasificacionTipoInstrumento" = case when v_Mercado  <> '0' then  v_Mercado else  "CMC"."IdClasificacionTipoInstrumento" end
		; 
		
		END IF;	
		
		--condicion 1.3 , sólo filtros rut 
		IF (v_CondFiltrosRut > 0) AND  (v_CondFiltrosNemo = 0) THEN 
		
		
		v_FacturacionFiltradosFecha = 
		select  
		"CMC"."IdMovimientoComprobante" as "IdMovimientoComprobante",
		"CMC"."DocumentoIdentidad" as "DocumentoIdentidadRut",
		"CMC"."FechaTransaccion" as "FechaTransaccion"
		from "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" as "CMC"
		where
		CAST("CMC"."FechaTransaccion" AS DATE)  >= v_ParametrosSolicitudFini
		and CAST("CMC"."FechaTransaccion" AS DATE)  <= IFNULL (v_ParametrosSolicitudFter,"CMC"."FechaTransaccion" )
		and (ltrim(rtrim(substring("CMC"."DocumentoIdentidad",1,LENGTH("CMC"."DocumentoIdentidad")-2)))) in (select distinct "PSR"."Valor" from :v_ParametrosSolicitudRut as "PSR" )
		and "CMC"."IdClasificacionTipoInstrumento" = case when v_Mercado  <> '0' then  v_Mercado else  "CMC"."IdClasificacionTipoInstrumento" end
		; 
		
		
		END IF;	
		
		
		--condicion 1.4 , sin filtros ingresados
		IF (v_CondFiltrosRut = 0) AND  (v_CondFiltrosNemo = 0) THEN 
		
		
		v_FacturacionFiltradosFecha = 
		select  
		"CMC"."IdMovimientoComprobante" as "IdMovimientoComprobante",
		"CMC"."DocumentoIdentidad" as "DocumentoIdentidadRut",
		"CMC"."FechaTransaccion" as "FechaTransaccion"
		from "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" as "CMC"
		where
		CAST("CMC"."FechaTransaccion" AS DATE)  >= v_ParametrosSolicitudFini
		and CAST("CMC"."FechaTransaccion" AS DATE)  <= IFNULL (v_ParametrosSolicitudFter,"CMC"."FechaTransaccion" )
		and "CMC"."IdClasificacionTipoInstrumento" = case when v_Mercado  <> '0' then  v_Mercado else  "CMC"."IdClasificacionTipoInstrumento" end
		; 
		
		
		END IF;	
		
END IF;	

--segunda condición, botón todos rut marcado
IF (v_TRut = 1) AND  (v_TNemo = 0) THEN 

		--condicion 2.1 , filtro nemo ingresado
		IF (v_CondFiltrosNemo > 0) THEN 
		
		
	v_FacturacionFiltradosFecha = 
		select  
		"CMC"."IdMovimientoComprobante" as "IdMovimientoComprobante",
		"CMC"."DocumentoIdentidad" as "DocumentoIdentidadRut",
		"CMC"."FechaTransaccion" as "FechaTransaccion"
		from "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" as "CMC"
		where
		CAST("CMC"."FechaTransaccion" AS DATE)  >= v_ParametrosSolicitudFini
		and CAST("CMC"."FechaTransaccion" AS DATE)  <= IFNULL (v_ParametrosSolicitudFter,"CMC"."FechaTransaccion")
		and "CMC"."NemotecnicoSubyacente"  in (select distinct "PSN"."Valor" from :v_ParametrosSolicitudNemo as "PSN")
		and "CMC"."IdClasificacionTipoInstrumento" = case when v_Mercado  <> '0' then  v_Mercado else  "CMC"."IdClasificacionTipoInstrumento" end
		; 
		
		END IF;	
		
		--condicion 2.2 , filtro nemo no ingresado
		IF (v_CondFiltrosNemo = 0) THEN 
		
			
		v_FacturacionFiltradosFecha = 
		select  
		"CMC"."IdMovimientoComprobante" as "IdMovimientoComprobante",
		"CMC"."DocumentoIdentidad" as "DocumentoIdentidadRut",
		"CMC"."FechaTransaccion" as "FechaTransaccion"
		from "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" as "CMC"
		where
		"CMC"."FechaTransaccion"  >= v_ParametrosSolicitudFini
		and "CMC"."FechaTransaccion"  <= IFNULL (v_ParametrosSolicitudFter,"CMC"."FechaTransaccion")
		and "CMC"."IdClasificacionTipoInstrumento" = case when v_Mercado  <> '0' then  v_Mercado else  "CMC"."IdClasificacionTipoInstrumento" end
		; 
		
		END IF;	
		
		
		
		
END IF;	

--tercera condición, botón todos nemo marcado
IF (v_TNemo = 1) AND (v_TRut = 0) THEN 
		
		--condicion 3.1 , filtro rut ingresado
		IF (v_CondFiltrosRut > 0) THEN 
		
		v_FacturacionFiltradosFecha = 
		select  
		"CMC"."IdMovimientoComprobante" as "IdMovimientoComprobante",
		"CMC"."DocumentoIdentidad" as "DocumentoIdentidadRut",
		"CMC"."FechaTransaccion" as "FechaTransaccion"
		from "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" as "CMC"
		where
		CAST("CMC"."FechaTransaccion" AS DATE)  >= v_ParametrosSolicitudFini
		and CAST("CMC"."FechaTransaccion" AS DATE)  <= IFNULL (v_ParametrosSolicitudFter,"CMC"."FechaTransaccion" )
		and (ltrim(rtrim(substring("CMC"."DocumentoIdentidad",1,LENGTH("CMC"."DocumentoIdentidad")-2)))) in (select distinct "PSR"."Valor" from :v_ParametrosSolicitudRut as "PSR" )
		and "CMC"."IdClasificacionTipoInstrumento" = case when v_Mercado  <> '0' then  v_Mercado else  "CMC"."IdClasificacionTipoInstrumento" end
		; 
		
		END IF;	
		
		--condicion 3.2, sin filtro rut
		IF (v_CondFiltrosRut = 0) THEN 
		
	v_FacturacionFiltradosFecha = 
		select  
		"CMC"."IdMovimientoComprobante" as "IdMovimientoComprobante",
		"CMC"."DocumentoIdentidad" as "DocumentoIdentidadRut",
		"CMC"."FechaTransaccion" as "FechaTransaccion"
		from "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" as "CMC"
		where
		CAST("CMC"."FechaTransaccion" AS DATE)  >= v_ParametrosSolicitudFini
		and CAST("CMC"."FechaTransaccion" AS DATE)  <= IFNULL (v_ParametrosSolicitudFter,"CMC"."FechaTransaccion" )
		and "CMC"."IdClasificacionTipoInstrumento" = case when v_Mercado  <> '0' then  v_Mercado else  "CMC"."IdClasificacionTipoInstrumento" end
		; 
		
		END IF;	

END IF;	


--cuarta condición, botónes todos marcados
IF (v_TRut = 1) AND (v_TNemo = 1) THEN 
		
		v_FacturacionFiltradosFecha = 
		select  
		"CMC"."IdMovimientoComprobante" as "IdMovimientoComprobante",
		"CMC"."DocumentoIdentidad" as "DocumentoIdentidadRut",
		"CMC"."FechaTransaccion" as "FechaTransaccion"
		from "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" as "CMC"
		where
		CAST("CMC"."FechaTransaccion" AS DATE)  >= v_ParametrosSolicitudFini
		and CAST("CMC"."FechaTransaccion" AS DATE)  <= IFNULL (v_ParametrosSolicitudFter,"CMC"."FechaTransaccion" )
		and "CMC"."IdClasificacionTipoInstrumento" = case when v_Mercado  <> '0' then  v_Mercado else  "CMC"."IdClasificacionTipoInstrumento" end
		; 	
		
END IF;

select count(1) into v_CondFinal from :v_FacturacionFiltradosFecha;

--select * from :v_AsignacionFiltradosFecha;

--select v_CondFinal as "condicion_final",v_TRut as "TodosRut", v_TNemo as "TodosNemo",v_ParametrosSolicitudFini as "fecini",v_ParametrosSolicitudFter as "fecter"  from dummy;



IF (v_CondFinal > 0) THEN 

		p_out_TablaSalida=
		select 
		"CMC"."DocumentoIdentidad" as "Rut",
		"CMC"."IdPortafolio" as "Cuenta",
		"CMC"."NombreLargoCliente" as "Nombre",
		"CMC"."IdBolsa" as "Bolsa",
		"CMC"."IdTransaccion" as "Folio",
		"CMC"."NemotecnicoInstrumento" as "Instrumento",
		LEFT(IFNULL("CMC"."IdTipoOperacion",''),1) as "TipoOperacion",
		"CMC"."IdOrden" as "NumeroOrden",
		"CMC"."IdComprobante" as "NumeroFactura",
		"CMC"."Cantidad" as "Cantidad",
		"CMC"."Precio" as "Precio",
		"CMC"."Monto" as "Monto",
		"CMC"."IdMoneda" as "Moneda",
		"CMC"."COMMVTOCMBTE" as "Comision",
		"CMC"."DERMVTOCMBTE" as "DerechoBolsa",
		"CMC"."GASMVTOCMBTE" as "Gastos",
		"CMC"."IMPMVTOCMBTE" as "Impuesto"
		from "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" as "CMC"
		INNER JOIN :v_FacturacionFiltradosFecha as "FFF" on "CMC"."IdMovimientoComprobante" = "FFF"."IdMovimientoComprobante";

END IF;	

SELECT * FROM  :p_out_TablaSalida;

END;















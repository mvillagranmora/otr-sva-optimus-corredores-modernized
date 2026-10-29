SET SCHEMA "BSCL";  
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::NCG380Ope";
--call "BSCL.Normativa.BD.Servicios.NOR::NCG380Ope"(390,?)
GO

		 	
		 	
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::NCG380Ope"
( 
    -- Parametros de Entrada
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100))
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida 			"BSCL.Normativa.BD::NOR.SALIDA_NCG380OPE"

     
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
	AS 
BEGIN

--DECLARACIONES
  
	DECLARE v_IdDocumento,v_IdEmpresa, v_Count INT:=0;
	DECLARE v_FechaOperacionDesde, v_FechaOperacionHasta NVARCHAR(10):='';


	IF p_IdSolicitudCircular>0 THEN
		
--obtengo id doc para traer IdEmpresa

		SELECT TOP 1 "IdDocumento" INTO v_IdDocumento 
		FROM "BSCL.Normativa.BD::NOR.SolicitudCircular"
		WHERE "IdSolicitudCircular" = p_IdSolicitudCircular;
		
		SELECT TOP 1 "IdEmpresa" INTO v_IdEmpresa 
		FROM "BSCL.Normativa.BD::DOC.Documento"
		WHERE "IdDocumento" = v_IdDocumento;
	
		
--obtengo datos filtros fechas 

		v_Datos = SELECT 
			  CASE WHEN SOL."TipoParametro"='FINI'	THEN "Valor" ELSE NULL END AS "FINI"
			, CASE WHEN SOL."TipoParametro"='FTER'	THEN "Valor" ELSE NULL END AS "FTER"
			FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" SOL
		 	WHERE SOL."IdSolicitudCircular" = p_IdSolicitudCircular;
			
	END IF;
		
/*	--Valores tabla parametros
	SELECT  COUNT(1) INTO v_Count FROM :p_TablaParametros;			
	if v_Count>0 THEN
		SELECT TOP 1  "Valor" INTO v_IdEmpresa
			FROM :p_TablaParametros SOL WHERE SOL."TipoParametro"='IdEmpresa';
			
		v_Datos = SELECT 
			  CASE WHEN SOL."TipoParametro"='FINI'	THEN "Valor" ELSE NULL END AS "FINI"
			, CASE WHEN SOL."TipoParametro"='FTER'	THEN "Valor" ELSE NULL END AS "FTER"
			FROM :p_TablaParametros SOL;
	END IF;	
*/

--asigno valores de filtro fechas
	
	v_DatosAgrupado =SELECT  
					  MAX(SOL."FINI") 	AS "FINI"
					, MAX(SOL."FTER")	AS "FTER"
					FROM :v_Datos SOL;
		
	SELECT TOP 1 "FINI","FTER"  INTO v_FechaOperacionDesde,v_FechaOperacionHasta FROM :v_DatosAgrupado;	
	
--select :v_FechaOperacionDesde,:v_FechaOperacionHasta from dummy;
		
	CALL "BSCL.Normativa.BD.Servicios.NCG380::NCG380Ope"
	(
		p_IdEmpresa 			=>v_IdEmpresa,
		p_FechaOperacionDesde 	=>v_FechaOperacionDesde,
		p_FechaOperacionHasta 	=>v_FechaOperacionHasta,
		p_out_TablaNCG380Ope 	=>v_out_TablaNCG380Ope
	);
	
	p_out_TablaSalida = 
		SELECT
			"NombreCliente",
			"RutCliente",
			"CodigoPortafolio",
			"CodigoBolsa",
			"Folio",
			"Instrumento",
			"TipoOperacion",
			"IdOperacion",
			"IdBolsa",
			"Fecha",
			"Hora",
			"NumeroOperador",
			"ISIN",
			"Nemotecnico",
			"TipoInstrumento",
			"TipoOperacionCod",
			"CondicionLiquidacion",
			"Cantidad",
			"MontoTotal",
			"Moneda",
			"Precio",
			"Mercado",
			"SistemaTransaccion",
			"ModalidadEjecucion",
			"RutContraparte",
			"DigitoContraparte",
			"Pasaporte",
			"GLEI",
			"Observaciones"
		FROM :v_out_TablaNCG380Ope;
		
		SELECT * FROM  :p_out_TablaSalida;
	

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

select 
'Juanito' as "Nombre",
'15222555-3' as "Rut",
'112233' as "Cuenta",
'BCS' as "Bolsa",
123456 as "Folio",
'LAN' as "Instrumento",
'C' as "TipoOperacion",
101010 as "NumeroOperacion",
'BCS' as "IdBolsa", --para operaciones en bolsa local, mostrar folio
'01-01-2015' as "Fecha",
'10:00' as "Hora",
33 as "NumeroOperador",
'isin' as "Isin",
'LAN' as "Nemotecnico",
'ACCN' as "TipoInstrumento", --necesario homologar
'CO' as "TipoOperacion",
'PH' as "CondicionLiquidacion",
100 as "Cantidad",
1000 as "MontoTotal",
'CLP' as "Moneda",
10 as "Precio",
01 as "Mercado",
01 as "SistemaTransaccion",
'OOI' as "ModalidadEjecucion",
'15555666' as "RutContraparte",
'K' as "DigContraparte",
111444777 as "Pasaporte",
'glei' as "Glei",
'observaciones' as "Observaciones"
from dummy;

p_out_TablaSalida=SELECT '' AS "Salida"  FROM  DUMMY;
*/

END;

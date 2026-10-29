SET SCHEMA "BSCL";
 GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::CI63Transa";
 GO
CREATE PROCEDURE 
--
--  Proposito: Servicio para la generacion del archivo por JOB Manager .
--  Autor: CFLORES - BCS
--  Fecha Creacion: 03/08/2016

"BSCL.Normativa.BD.Servicios.NOR::CI63Transa" ( 
    --VARIABLES DEL PROCESO
	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100))
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	"BSCL.Normativa.BD::NOR.SALIDA_CIR63TRANSAC" --Se debe llamar siemte p_out_TablaSalida
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		
		DECLARE v_IdDocumento,v_IdEmpresa, v_Count INT:=0;
		DECLARE v_FechaOperacionDesde, v_FechaOperacionHasta NVARCHAR(10):='';
		
		DECLARE v_IdClasificacionTipoInstrumento, v_TipoInformacion		NVARCHAR(3):='';
		DECLARE v_IdEmisor			       			  	NVARCHAR(40):='';
		DECLARE v_IdInstrumento							BIGINT:=0;
		DECLARE v_IdCliente 							BIGINT:=0;	
		
		
		IF p_IdSolicitudCircular>0 THEN
			SELECT TOP 1 "IdDocumento" INTO v_IdDocumento 
			FROM "BSCL.Normativa.BD::NOR.SolicitudCircular"
			WHERE "IdSolicitudCircular" = p_IdSolicitudCircular;
			
			SELECT TOP 1 "IdEmpresa" INTO v_IdEmpresa 
			FROM "BSCL.Normativa.BD::DOC.Documento"
			WHERE "IdDocumento" = v_IdDocumento;
			
			v_Datos = SELECT 
				  CASE WHEN SOL."TipoParametro"='TIPINF'	THEN "Valor" ELSE NULL END AS "TIPINF"
				, CASE WHEN SOL."TipoParametro"='FINI'		THEN "Valor" ELSE NULL END AS "FINI"
				, CASE WHEN SOL."TipoParametro"='FTER'		THEN "Valor" ELSE NULL END AS "FTER"
				, CASE WHEN SOL."TipoParametro"='CLAINS'	THEN "Valor" ELSE NULL END AS "CLAINS"
				, CASE WHEN SOL."TipoParametro"='IDEMI'		THEN "Valor" ELSE NULL END AS "IDEMI"
				, CASE WHEN SOL."TipoParametro"='IDINS'		THEN "Valor" ELSE NULL END AS "IDINS"						
				, CASE WHEN SOL."TipoParametro"='IDCLI'		THEN "Valor" ELSE NULL END AS "IDCLI"			
				FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" SOL
			 	WHERE SOL."IdSolicitudCircular" = p_IdSolicitudCircular;
		END IF;	
	
		--Valores tabla parametros desde UI VISOR
		SELECT  COUNT(1) INTO v_Count FROM :p_TablaParametros;			
		if v_Count>0 THEN
			SELECT TOP 1  "Valor" INTO v_IdEmpresa
				FROM :p_TablaParametros SOL WHERE SOL."TipoParametro"='IdEmpresa';
				
			v_Datos = SELECT
				  CASE WHEN SOL."TipoParametro"='TIPINF'		THEN "Valor" ELSE NULL END AS "TIPINF"
				, CASE WHEN SOL."TipoParametro"='fechaDesde'	THEN "Valor" ELSE NULL END AS "FINI"
				, CASE WHEN SOL."TipoParametro"='fechaHasta'	THEN "Valor" ELSE NULL END AS "FTER"
				, CASE WHEN SOL."TipoParametro"='CLAINS'		THEN "Valor" ELSE NULL END AS "CLAINS"
				, CASE WHEN SOL."TipoParametro"='IDEMI'			THEN "Valor" ELSE NULL END AS "IDEMI"
				, CASE WHEN SOL."TipoParametro"='IDINS'			THEN "Valor" ELSE NULL END AS "IDINS"						
				, CASE WHEN SOL."TipoParametro"='IDCLI'			THEN "Valor" ELSE NULL END AS "IDCLI"					
				FROM :p_TablaParametros SOL;
		END IF;
		
	
	
		v_DatosAgrupado =SELECT
							  MAX(SOL."TIPINF") 	AS "TIPINF"
							, MAX(SOL."FINI") 		AS "FINI"
							, MAX(SOL."FTER")		AS "FTER"
							, MAX(SOL."CLAINS")		AS "CLAINS"
							, MAX(SOL."IDEMI")		AS "IDEMI"
							, MAX(SOL."IDINS")		AS "IDINS"																					
							, MAX(SOL."IDCLI")		AS "IDCLI"										
							FROM :v_Datos  SOL;
						

		SELECT TOP 1 "FINI","FTER","TIPINF","CLAINS","IDEMI","IDINS","IDCLI"  INTO v_FechaOperacionDesde,v_FechaOperacionHasta, v_TipoInformacion
		,v_IdClasificacionTipoInstrumento, v_IdEmisor, v_IdInstrumento, v_IdCliente FROM :v_DatosAgrupado;	
		v_FechaOperacionDesde:=COALESCE(v_FechaOperacionDesde,'');
		v_FechaOperacionHasta:=COALESCE(v_FechaOperacionHasta,'');
		v_TipoInformacion:=COALESCE(v_TipoInformacion,'AM');		
		v_IdClasificacionTipoInstrumento:=COALESCE(v_IdClasificacionTipoInstrumento,'');
		v_IdEmisor:=COALESCE(v_IdEmisor,'');
		v_IdInstrumento:=COALESCE(v_IdInstrumento,0);		
		v_IdCliente:=COALESCE(v_IdCliente,0);		
						
		CALL "BSCL.Normativa.BD.Servicios.CIR63::ConsultarTransacciones"
		(	p_IdEmpresa 						=>v_IdEmpresa,
			p_TipoInformacion 					=>v_TipoInformacion,
			p_FechaOperacionDesde 				=>v_FechaOperacionDesde,
			p_FechaOperacionHasta 				=>v_FechaOperacionHasta,
			p_IdClasificacionTipoInstrumento 	=>v_IdClasificacionTipoInstrumento,
			p_IdEmisor 							=>v_IdEmisor,
			p_IdInstrumento 					=>v_IdInstrumento,			
			p_IdCliente 						=>v_IdCliente,				
			p_out_TablaTransac 					=>v_out_TablaTransac
		); 
		p_out_TablaSalida = SELECT "NombreDestinatario"
		,"CargoDestinatario"
		--,"IdTipoOperacion"
		--,TO_DATE ("FechaOperacion", 'YYYY-MM-DD') AS "FechaOperacion"
		,"FechaOperacion"
		,"IdTipoOperacion"
		,"NombreTipoOperacion"
		,"IdClasificacionTipoInstrumento"
		,"IdInstrumento"
		,"Nemotecnico"
		,"Cantidad"
		,"Precio"
		,"Monto"
		,"IdMoneda"
		,"Moneda"
		,"IdCliente"
		,"NombreCliente"
		,"DocumentoIdentidad"
		,"TipoDocumentoIdentidad"
		,"NombreTipoDocumentoIdentidad"
		,"IdTipoRelacion"
		,"NombreTipoRelacion"
		,"CodigoRelacionNorma"
		,"DescripcionRelacionNorma"
		,"IdOperacion"
		,"Mes"		
		 FROM :v_out_TablaTransac;
		 
		SELECT * FROM  :p_out_TablaSalida;		 
		
END; 

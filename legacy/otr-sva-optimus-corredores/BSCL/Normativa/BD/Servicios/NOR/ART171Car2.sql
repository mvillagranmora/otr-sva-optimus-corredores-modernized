SET SCHEMA "BSCL";
 GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::ART171Car2";
 GO
CREATE PROCEDURE 
--
--  Proposito: Servicio para la generacion del archivo por JOB Manager .
--  Autor: CFLORES - BCS
--  Fecha Creacion: 27/07/2016

--  CELBUHOPRO-1101: Se agrega nueva tabla type especificamente para el archivo de salida de Carta2 .
--  Autor: CLATORRE - BCS
--  Fecha Creacion: 04/12/2023

"BSCL.Normativa.BD.Servicios.NOR::ART171Car2" ( 
    --VARIABLES DEL PROCESO
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100))
    --VARIABLES DE SALIDA 
 	,OUT p_out_TablaSalida	"BSCL.Normativa.BD::NOR.SALIDA_ART171CARTA2EXP" --Se debe llamar siemte p_out_TablaSalida
	--,OUT p_out_TablaCarta2	 "BSCL.Normativa.BD::NOR.SALIDA_ART171_C2"
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		
		DECLARE v_IdDocumento,v_IdEmpresa, v_Count INT:=0;
		DECLARE v_FechaOperacionDesde, v_FechaOperacionHasta NVARCHAR(10):='';
			
		IF p_IdSolicitudCircular>0 THEN
			SELECT TOP 1 "IdDocumento" INTO v_IdDocumento 
			FROM "BSCL.Normativa.BD::NOR.SolicitudCircular"
			WHERE "IdSolicitudCircular" = p_IdSolicitudCircular;
			
			SELECT TOP 1 "IdEmpresa" INTO v_IdEmpresa 
			FROM "BSCL.Normativa.BD::DOC.Documento"
			WHERE "IdDocumento" = v_IdDocumento;
			
			
			v_Datos = SELECT 
				  CASE WHEN SOL."TipoParametro"='FINI'	THEN "Valor" ELSE NULL END AS "FINI"
				, CASE WHEN SOL."TipoParametro"='FTER'	THEN "Valor" ELSE NULL END AS "FTER"
				FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" SOL
			 	WHERE SOL."IdSolicitudCircular" = p_IdSolicitudCircular;
		END IF;			
	
	
		SELECT  COUNT(1) INTO v_Count FROM :p_TablaParametros;			
		if v_Count>0 THEN
			SELECT TOP 1  "Valor" INTO v_IdEmpresa
				FROM :p_TablaParametros SOL WHERE SOL."TipoParametro"='IdEmpresa';
				
			v_Datos = SELECT 
				  CASE WHEN SOL."TipoParametro"='fechaDesde'	THEN "Valor" ELSE NULL END AS "FINI"
				, CASE WHEN SOL."TipoParametro"='fechaHasta'	THEN "Valor" ELSE NULL END AS "FTER"
				FROM :p_TablaParametros SOL;
		END IF;
		
	
		v_DatosAgrupado =SELECT 
							  MAX(SOL."FINI") 	AS "FINI"
							, MAX(SOL."FTER")	AS "FTER"
							FROM :v_Datos SOL;
						

		SELECT TOP 1"FINI","FTER"  INTO v_FechaOperacionDesde,v_FechaOperacionHasta FROM :v_DatosAgrupado;	
				
	
		CALL "BSCL.Normativa.BD.Servicios.ART171::ConsultarCartaDos"
		(	p_IdEmpresa 			=>v_IdEmpresa,
			p_FechaOperacionDesde 	=>TO_VARCHAR (:v_FechaOperacionDesde, 'YYYY-MM-DD'),
			p_FechaOperacionHasta 	=>TO_VARCHAR (:v_FechaOperacionHasta, 'YYYY-MM-DD'),
			p_out_TablaCarta2 		=>v_out_TablaCarta2
		); 

		p_out_TablaSalida=SELECT 
		"NombreDestinatario"
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
		,"MesSistema"				    
		,"MesConsulta"
		,"YearSistema"
		,"YearConsulta"
		,"DiaSistema"
		,"DiaConsulta"
		 FROM :v_out_TablaCarta2;
		 
		SELECT * FROM  :p_out_TablaSalida ORDER BY "IdCliente","FechaOperacion","Nemotecnico","IdTipoOperacion";
END; 

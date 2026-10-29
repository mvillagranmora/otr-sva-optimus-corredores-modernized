SET SCHEMA "BSCL";
 
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::CI63Nomina";
 
CREATE PROCEDURE 
--
--  Proposito: Servicio para la generacion del archivo por JOB Manager .
--  Autor: CFLORES - BCS
--  Fecha Creacion: 08/08/2016

"BSCL.Normativa.BD.Servicios.NOR::CI63Nomina" ( 
    --VARIABLES DEL PROCESO
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100))
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	"BSCL.Normativa.BD::NOR.SALIDA_CIR63NOMINAJOB" --Se debe llamar siemte p_out_TablaSalida
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		
		DECLARE v_IdDocumento,v_IdEmpresa, v_Count INT:=0;
		DECLARE v_TipoInformacion NVARCHAR(3):='';
			
		
		IF p_IdSolicitudCircular>0 THEN
		
			SELECT TOP 1 "IdDocumento" INTO v_IdDocumento 
			FROM "BSCL.Normativa.BD::NOR.SolicitudCircular"
			WHERE "IdSolicitudCircular" = p_IdSolicitudCircular;
			
			SELECT TOP 1 "IdEmpresa" INTO v_IdEmpresa 
			FROM "BSCL.Normativa.BD::DOC.Documento"
			WHERE "IdDocumento" = v_IdDocumento;
		
			v_Datos = SELECT  CASE WHEN SOL."TipoParametro"='TIPINF'	THEN "Valor" ELSE NULL END AS "TIPINF"
				FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" SOL
			 	WHERE SOL."IdSolicitudCircular" = p_IdSolicitudCircular;
		END IF;		
	
			--Valores tabla parametros
		SELECT  COUNT(1) INTO v_Count FROM :p_TablaParametros;			
		if v_Count>0 THEN
			SELECT TOP 1  "Valor" INTO v_IdEmpresa
				FROM :p_TablaParametros SOL WHERE SOL."TipoParametro"='IdEmpresa';
				
			v_Datos = SELECT 
				  CASE WHEN SOL."TipoParametro"='TIPINF'	THEN "Valor" ELSE NULL END AS "TIPINF"
				FROM :p_TablaParametros SOL;
		END IF;
		
	
		v_DatosAgrupado =SELECT MAX(SOL."TIPINF") 	AS "TIPINF"
							FROM :v_Datos SOL;
					

		SELECT TOP 1 "TIPINF"  INTO v_TipoInformacion FROM :v_DatosAgrupado;	
				
	
	
		CALL "BSCL.Normativa.BD.Servicios.CIR63::ConsultarNominaRelacionados"
		(	p_IdEmpresa 			=>v_IdEmpresa,
			p_TipoInformacion 		=>v_TipoInformacion,
			p_out_TablaNomina 		=>v_out_TablaNomina
		); 
		
		p_out_TablaSalida=
		SELECT
		     "NombreDestinatario"
			,"CargoDestinatario"
			,"Direccion"
			,"Comuna"
			,"Ciudad"
 			,"IdPersona"
			,"NombrePersona"
  			,"IdPersonaEmpleador"
 			,"NombreEmpleador"
			,"DocumentoIdentidad"
			,"TipoDocumentoIdentidad"
			,"NombreTipoDocumentoIdentidad"
			,"IdTipoRelacion"
			,"NombreTipoRelacion"
			,"CodigoRelacionNorma"
			,"DescripcionRelacionNorma"
			,"Periodo"
		 FROM :v_out_TablaNomina;
		
END; 

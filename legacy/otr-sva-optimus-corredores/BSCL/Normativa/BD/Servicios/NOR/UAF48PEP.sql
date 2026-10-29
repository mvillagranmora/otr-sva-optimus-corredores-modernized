SET SCHEMA "BSCL";
 
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::UAF48PEP";
 
CREATE PROCEDURE 
--
--  Proposito: Servicio para la generacion del archivo por JOB Manager .
--  Autor: CFLORES - BCS
--  Fecha Creacion: 06/09/2016

"BSCL.Normativa.BD.Servicios.NOR::UAF48PEP" ( 
    --VARIABLES DEL PROCESO
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100)) 
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	"BSCL.Normativa.BD::NOR.SALIDA_UAFCIR63PEP" 
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
					
		END IF;

		CALL "BSCL.Normativa.BD.Servicios.UAF::ConsultarPEP"
		(	p_IdEmpresa 			=>v_IdEmpresa,
			p_out_TablaPEP 			=>v_out_TablaPEP
		); 
		
			
		p_out_TablaSalida=SELECT 
									"IdPersona",
									"IdCliente",
									"CodigoPortafolio",
									"NombreCliente",
									"RutCliente",
									"CodigoEjecutivo",
									"NombreEjecutivo",
									"FechaConsulta",
									"Titulos"
		 FROM :v_out_TablaPEP;
		 
		SELECT * FROM  :p_out_TablaSalida;
END; 

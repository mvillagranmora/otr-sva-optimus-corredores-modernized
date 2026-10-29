SET SCHEMA "BSCL";
 
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::Anexo1";
 
CREATE PROCEDURE 


"BSCL.Normativa.BD.Servicios.NOR::Anexo1" ( 
    --VARIABLES DEL PROCESO
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100)) 
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	"BSCL.Normativa.BD::NOR.SALIDA_ANEXO1"
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		
		DECLARE v_IdDocumento,v_IdEmpresa, v_Count INT:=0;
		DECLARE v_Fecha NVARCHAR(10):='';

		IF p_IdSolicitudCircular>0 THEN
		
			SELECT TOP 1 "IdDocumento" INTO v_IdDocumento 
			FROM "BSCL.Normativa.BD::NOR.SolicitudCircular"
			WHERE "IdSolicitudCircular" = p_IdSolicitudCircular;
			
			SELECT TOP 1 "IdEmpresa" INTO v_IdEmpresa 
			FROM "BSCL.Normativa.BD::DOC.Documento"
			WHERE "IdDocumento" = v_IdDocumento;
		
			
			
			v_Datos = SELECT 
				  CASE WHEN SOL."TipoParametro"='FINI'	THEN "Valor" ELSE NULL END AS "FINI"
				FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" SOL
			 	WHERE SOL."IdSolicitudCircular" = p_IdSolicitudCircular;
				
		END IF;
		
		--Valores tabla parametros
		SELECT  COUNT(1) INTO v_Count FROM :p_TablaParametros;			
		if v_Count>0 THEN
			SELECT TOP 1  "Valor" INTO v_IdEmpresa
				FROM :p_TablaParametros SOL WHERE SOL."TipoParametro"='IdEmpresa';
				
			v_Datos = SELECT 
				  CASE WHEN SOL."TipoParametro"='fechaDesde'	THEN "Valor" ELSE NULL END AS "FINI"
				FROM :p_TablaParametros SOL;
		END IF;
		
		
			v_DatosAgrupado =SELECT  
							  MAX(SOL."FINI") 	AS "FINI"
							FROM :v_Datos SOL;
							
	
			SELECT TOP 1 "FINI"  INTO v_Fecha FROM :v_DatosAgrupado;	
		
		
		CALL "BSCL.Normativa.BD.Servicios.Anexos::ConsultarDatosAnexoUno"
		(	p_IdEmpresa 			=>v_IdEmpresa,
			p_Fecha 		    	=>v_Fecha,
			p_out_TablaAnexo1 		=>v_out_TablaAnexo1
		); 
				
		
		p_out_TablaSalida=
		SELECT *
		FROM :v_out_TablaAnexo1;
		 
		SELECT * FROM  :p_out_TablaSalida;
END; 



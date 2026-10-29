SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::AgregarLogNormativo";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::AgregarLogNormativo"
( 
    -- Parametros de Entrada
	IN	p_IdEmpresa			NVARCHAR(200) default '',
	IN 	p_InfoDetalle       TABLE ("IdTipoLogNormativa"	BIGINT,"IdUsuario"	BIGINT,	"FechaLog"	NVARCHAR(40),"IdProceso" BIGINT,"IdSolicitudCircular" 	BIGINT,"IdOperacion" BIGINT,"Registro"	NVARCHAR(500),"Observacion"	NVARCHAR(500)),
    -- Salida
    OUT p_out_resultado						  INT
   
   -- Parametros de Salida
  /* OUT p_out_AdvertenciasContador      INT,
   OUT p_out_AdvertenciasDetalle       TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
   OUT p_out_ErroresContador           INT,   
   OUT p_out_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
   */      
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  
  
   	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
/*	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_errorcuenta 				INT DEFAULT 0;  
	DECLARE v_errorhashmsg 				TABLE ("Codigo" NVARCHAR(100),"Valor" NVARCHAR(5000));
	DECLARE v_erroroutmsg  				NVARCHAR(5000);		
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_ErroresDetalle			TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520));
  
	declare v_Secuencia int;
	
	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT '' as "Codigo",'' as "Valor" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_ErroresContador
		FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador 
			FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;
	
	
	DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
	

	p_out_AdvertenciasContador		:= 0;
	p_out_ErroresContador			:= 0;
	p_out_AdvertenciasDetalle		= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;  	
	p_out_ResultadoProceso			= SELECT '' as "Campo", 0 as "Id", '' as "Servicio", '' as "Resultado" FROM dummy where 1=2;
	v_ErroresDetalle				= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;

	select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
		IF :p_out_ErroresContador > 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;	

*/



	--------------------
	--LOGICA
	--------------------

insert into "BSCL.Normativa.BD::NOR.LogNormativa"
select
--(select IFNULL(MAX("LOG"."IdLogNormativa"),0)+1 from "BSCL.Normativa.BD::NOR.LogNormativa" as "LOG") as "IdLogNormativa",
 "BSCL.Normativa.BD.Secuencias::LogNormativo".NEXTVAL ,
"IdTipoLogNormativa" as "IdTipoLogNormativa", 
"IdUsuario" as "IdUsuario",            
"FechaLog" as "FechaLog",                 
"IdProceso" as "IdProceso",                    
"IdSolicitudCircular" as "IdSolicitudCircular",       
"IdOperacion" as "IdOperacion",        
"Registro" as "Registro",     
"Observacion" as "Observacion"
from :p_InfoDetalle;



SELECT ::ROWCOUNT INTO p_out_resultado FROM DUMMY;

END;
SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Homologacion.BD.Servicios::EnhControlIntegridad_IF";
CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::EnhControlIntegridad_IF"
	(  
			IN  p_IdCarga 	       	 BIGINT,         	-- ID de la carga 
			IN  p_Periodo_Inicial    NVARCHAR(07)   	--YYYY-MM-DD
    ) 
		LANGUAGE SQLSCRIPT
		SQL SECURITY INVOKER 
		DEFAULT SCHEMA "BSCL"
		---READS SQL DATA 
	AS   
	BEGIN
   
	    -- Para LOg
	    DECLARE v_out_refID				BIGINT;    
	    DECLARE v_out_ErroresContador	BIGINT; 
	    DECLARE v_MaxPeriodo			NVARCHAR(5000);
	    DECLARE v_FechaInicioPeriodo 	NVARCHAR(5000);
	    DECLARE v_RutCli 				NVARCHAR(5000):='0-0';

	    DECLARE Concepto_Egre 			NVARCHAR(5000) default '';
	    DECLARE Concepto_Ingre 			NVARCHAR(5000) default '';

--	    DECLARE v_periodo  				DATE := CONCAT(SUBSTRING(p_Periodo_Inicial,1,7),'-01');
--		DECLARE v_periodo_ant  			DATE := CONCAT(SUBSTRING(p_Periodo_Inicial,1,7),'-01');


--------------------------------------------------------------------------------------------------------------------
------  						valida que no se procese 2 veces
--------------------------------------------------------------------------------------------------------------------
insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
           ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2",
            "ValorCampoGenerico2","NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,
            "NombreCampoGenerico5" ,"ValorCampoGenerico5","NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,
            "ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8","NombreCampoGenerico9" ,"ValorCampoGenerico9" ,
            "NombreCampoGenerico10","ValorCampoGenerico10")
			
     select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
            , p_IdCarga 					as "IdCarga"
            , 'INTEGRIDAD' 					as "Entidad"
            , 'TBCTCIIF Mismo p_IdCarga' 	as "Mensaje"
            , 'COD_PER' 					as "NombreCampoGenerico1"
            , GES_TBCTCIIF."COD_PER" 		as "ValorCampoGenerico1"
            , '' 							as "NombreCampoGenerico2"
            , '' 							as "ValorCampoGenerico2"
            , '' 							as "NombreCampoGenerico3"
            , '' 							as "ValorCampoGenerico3"
            , '' 							as "NombreCampoGenerico4"
            , '' 							as "ValorCampoGenerico4"
            , '' 							as "NombreCampoGenerico5"
            , '' 							as "ValorCampoGenerico5"
            , 'SEC_CAR_INVER' 				as "NombreCampoGenerico6"
            , GES_TBCTCIIF."SEC_CAR_INVER" 	as "ValorCampoGenerico6"
            , '' 							as "NombreCampoGenerico7"
            , 0 							as "ValorCampoGenerico7"
            , '' 							as "NombreCampoGenerico8"
            , 0 							as "ValorCampoGenerico8"
            , '' 							as "NombreCampoGenerico9"
            , '0001-01-01' 					as "ValorCampoGenerico9"
            , '' 							as "NombreCampoGenerico10"
            , '0001-01-01' 					as "ValorCampoGenerico10"
       FROM "BSCL_VIRTUAL"."GES_TBCTCIIF" as  GES_TBCTCIIF
		    INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Cartera" AS Cartera
		    ON Cartera."COD_PER" 		= GES_TBCTCIIF."COD_PER" 				AND 
		       Cartera."RUT_CLI" 		= GES_TBCTCIIF."RUT_CLI" 				AND 
		       Cartera."SEC_RUT_CLI" 	= GES_TBCTCIIF."SEC_RUT_CLI" 			AND 
		       Cartera."SEC_CAR_INVER" 	= GES_TBCTCIIF."SEC_CAR_INVER" 
	 WHERE GES_TBCTCIIF."COD_PER" 		=  SUBSTRING(p_Periodo_Inicial,1,7)		AND 
		   GES_TBCTCIIF."COD_PER" 		NOT LIKE '%90%' 						AND 
		   (GES_TBCTCIIF."CAN_ACTUAL" 	< 0  or GES_TBCTCIIF."CAN_ACTUAL" > 0)	AND 
		   Cartera."IdCarga"			= p_IdCarga;
		        
-------------------------------------------------------------------------------	
------  		Valido Integridad por Documento Identidad
-------------------------------------------------------------------------------
insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
           ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2",
            "ValorCampoGenerico2","NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,
            "NombreCampoGenerico5" ,"ValorCampoGenerico5","NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,
            "ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8","NombreCampoGenerico9" ,"ValorCampoGenerico9" ,
            "NombreCampoGenerico10","ValorCampoGenerico10")
			
	select 	"BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL 	as "IdError"
			, p_IdCarga 						as "IdCarga"
			, 'INTEGRIDAD' 						as "Entidad"
			, 'No exite Documento Identidad' 	as "Mensaje"
			, 'RUT_CLI' 						as "NombreCampoGenerico1"
			, GES_TBCTCIIF."RUT_CLI" 			as "ValorCampoGenerico1"
			, '' 								as "NombreCampoGenerico2"
			, '' 								as "ValorCampoGenerico2"
			, '' 								as "NombreCampoGenerico3"
			, '' 								as "ValorCampoGenerico3"
			, '' 								as "NombreCampoGenerico4"
			, '' 								as "ValorCampoGenerico4"
			, '' 								as "NombreCampoGenerico5"
			, '' 								as "ValorCampoGenerico5"
			, '' 								as "NombreCampoGenerico6"
			, 0 								as "ValorCampoGenerico6"
			, '' 								as "NombreCampoGenerico7"
			, 0 								as "ValorCampoGenerico7"
			, '' 								as "NombreCampoGenerico8"
			, 0 								as "ValorCampoGenerico8"
			, '' 								as "NombreCampoGenerico9"
            , '0001-01-01' 						as "ValorCampoGenerico9"
            , '' 								as "NombreCampoGenerico10"
            , '0001-01-01' 						as "ValorCampoGenerico10"
			
	 FROM  	"BSCL_VIRTUAL"."GES_TBCTCIIF" as  GES_TBCTCIIF
      		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
			ON DocumentoIdentidad."DocumentoIdentidad" = UPPER(TRIM((GES_TBCTCIIF."RUT_CLI")))
	 WHERE 	DocumentoIdentidad."DocumentoIdentidad" IS NULL
	 GROUP  BY GES_TBCTCIIF."RUT_CLI";
		  
-------------------------------------------------------------------------------	
------  						Valido Integridad por PERSONA
-------------------------------------------------------------------------------	
insert into		"BSCL"."BSCL.Homologacion.BD::HOM.Error"
				("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
				
	select  "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
				, p_IdCarga 				as "IdCarga"
				, 'INTEGRIDAD' 				as "Entidad"
				, 'No existe Persona' 		as "Mensaje"
				, 'RUT_CLI' 				as "NombreCampoGenerico1"
				, GES_TBCTCIIF."RUT_CLI" 	as "ValorCampoGenerico1"
				, '' 						as "NombreCampoGenerico2"
				, '' 						as "ValorCampoGenerico2"
				, '' 						as "NombreCampoGenerico3"
				, '' 						as "ValorCampoGenerico3"
				, '' 						as "NombreCampoGenerico4"
				, '' 						as "ValorCampoGenerico4"
				, '' 						as "NombreCampoGenerico5"
				, '' 						as "ValorCampoGenerico5"
				, '' 						as "NombreCampoGenerico6"
				, 0 						as "ValorCampoGenerico6"
				, '' 						as "NombreCampoGenerico7"
				, 0 						as "ValorCampoGenerico7"
				, '' 						as "NombreCampoGenerico8"
				, 0 						as "ValorCampoGenerico8"
				, '' 						as "NombreCampoGenerico9"
				, '0001-01-01' 				as "ValorCampoGenerico9"
				, '' 						as "NombreCampoGenerico10"
				, '0001-01-01' 				as "ValorCampoGenerico10"
				FROM  "BSCL_VIRTUAL"."GES_TBCTCIIF" as  GES_TBCTCIIF
						inner JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
						ON UPPER(TRIM(DocumentoIdentidad."DocumentoIdentidad")) = UPPER(TRIM(GES_TBCTCIIF."RUT_CLI"))
						LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
						ON Persona."IdPersona" = DocumentoIdentidad."IdPersona"
				where Persona."IdPersona" is null;
-------------------------------------------------------------------------------	
------  	Valido Integridad por CLIENTE ASOCIADO
-------------------------------------------------------------------------------	
insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
			("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
			,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
			,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
			,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
					
	select  "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
			, p_IdCarga 					as "IdCarga"
			, 'INTEGRIDAD' 					as "Entidad"
			, 'No existe Cliente Asociado' 	as "Mensaje"
			, 'RUT_CLI' 					as "NombreCampoGenerico1"
			, GES_TBCTCIIF."RUT_CLI" 		as "ValorCampoGenerico1"
			, '' 							as "NombreCampoGenerico2"
			, '' 							as "ValorCampoGenerico2"
			, '' 							as "NombreCampoGenerico3"
			, '' 							as "ValorCampoGenerico3"
			, '' 							as "NombreCampoGenerico4"
			, '' 							as "ValorCampoGenerico4"
			, '' 							as "NombreCampoGenerico5"
			, '' 							as "ValorCampoGenerico5"
			, 'IdPersona' 					as "NombreCampoGenerico6"
			, Persona."IdPersona" 			as "ValorCampoGenerico6"
			, '' 							as "NombreCampoGenerico7"
			, 0 							as "ValorCampoGenerico7"
			, '' 							as "NombreCampoGenerico8"
			, 0 							as "ValorCampoGenerico8"
			, '' 							as "NombreCampoGenerico9"
			, '0001-01-01' 					as "ValorCampoGenerico9"
			, '' 							as "NombreCampoGenerico10"
			, '0001-01-01' 					as "ValorCampoGenerico10"
	  FROM  "BSCL_VIRTUAL"."GES_TBCTCIIF" as  GES_TBCTCIIF
			inner JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
			ON UPPER(TRIM(DocumentoIdentidad."DocumentoIdentidad")) = UPPER(TRIM(GES_TBCTCIIF."RUT_CLI"))
			inner JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
			ON Persona."IdPersona" = DocumentoIdentidad."IdPersona"
			left JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente
			ON Cliente."IdPersona" = Persona."IdPersona"	
	 where 	Cliente."IdCliente" is null;
	 
-------------------------------------------------------------------------------	
------  		Valido Integridad por PORTAFOLIO
-------------------------------------------------------------------------------	
insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
			("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
			,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
			,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
			,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
			
	select  "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL  as "IdError"
			, p_IdCarga 				as "IdCarga"
			, 'INTEGRIDAD' 				as "Entidad"
			, 'No existe Portafolio' 	as "Mensaje"
			, 'RUT_CLI' 				as "NombreCampoGenerico1"
			, GES_TBCTCIIF."RUT_CLI" 	as "ValorCampoGenerico1"
			, '' 						as "NombreCampoGenerico2"
			, '' 						as "ValorCampoGenerico2"
			, '' 						as "NombreCampoGenerico3"
			, '' 						as "ValorCampoGenerico3"
			, '' 						as "NombreCampoGenerico4"
			, '' 						as "ValorCampoGenerico4"
			, '' 						as "NombreCampoGenerico5"
			, '' 						as "ValorCampoGenerico5"
			, 'IdPersona' 				as "NombreCampoGenerico6"
			, Persona."IdPersona" 		as "ValorCampoGenerico6"
			, 'IdCliente' 				as "NombreCampoGenerico7"
			, Cliente."IdCliente" 		as "ValorCampoGenerico7"
			, '' 						as "NombreCampoGenerico8"
			, 0 						as "ValorCampoGenerico8"
			, '' 						as "NombreCampoGenerico9"
			, '0001-01-01' 				as "ValorCampoGenerico9"
			, '' 						as "NombreCampoGenerico10"
			, '0001-01-01' 				as "ValorCampoGenerico10"
			
	  FROM  "BSCL_VIRTUAL"."GES_TBCTCIIF" as  GES_TBCTCIIF
			inner JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
			ON UPPER(TRIM(DocumentoIdentidad."DocumentoIdentidad")) = UPPER(TRIM(GES_TBCTCIIF."RUT_CLI"))
			inner JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona
			ON Persona."IdPersona" = DocumentoIdentidad."IdPersona"
			inner JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente
			ON Cliente."IdPersona" = Persona."IdPersona"	
			left JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
			ON Portafolio."IdCliente" = Cliente."IdCliente"  
			AND Portafolio."CodigoPortafolio" = TO_VARCHAR(GES_TBCTCIIF."SEC_RUT_CLI")
	  where Portafolio."IdPortafolio" is null;
		 	 
-----------------------------------------------------------------------------------------------------------------------
------  						Valido Integridad por Instrumento
--------------------------------------------------------------------------------------------------------------------
insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
           ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2",
            "ValorCampoGenerico2","NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,
            "NombreCampoGenerico5" ,"ValorCampoGenerico5","NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,
            "ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8","NombreCampoGenerico9" ,"ValorCampoGenerico9" ,
            "NombreCampoGenerico10","ValorCampoGenerico10")
			
	select 	"BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
			, p_IdCarga 							as "IdCarga"
			, 'INTEGRIDAD' 				as "Entidad"
			, 'No exite Instrumento'	as "Mensaje"
			, 'NEMO' 					as "NombreCampoGenerico1"
			, GES_TBCTCIIF."NEMO" 		as "ValorCampoGenerico1"
			, '' 						as "NombreCampoGenerico2"
			, '' 						as "ValorCampoGenerico2"
			, '' 						as "NombreCampoGenerico3"
			, '' 						as "ValorCampoGenerico3"
			, '' 						as "NombreCampoGenerico4"
			, '' 						as "ValorCampoGenerico4"
			, '' 						as "NombreCampoGenerico5"
			, '' 						as "ValorCampoGenerico5"
			, '' 						as "NombreCampoGenerico6"
			, 0 						as "ValorCampoGenerico6"
			, '' 						as "NombreCampoGenerico7"
			, 0 						as "ValorCampoGenerico7"
			, '' 						as "NombreCampoGenerico8"
			, 0 						as "ValorCampoGenerico8"
			, '' 						as "NombreCampoGenerico9"
            , '0001-01-01' 				as "ValorCampoGenerico9"
            , '' 						as "NombreCampoGenerico10"
            , '0001-01-01' 				as "ValorCampoGenerico10"
			
	 FROM  	"BSCL_VIRTUAL"."GES_TBCTCIIF" as  GES_TBCTCIIF
			LEFT  JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
			ON TRIM((Instrumento."Nemotecnico")) = TRIM((GES_TBCTCIIF."NEMO"))
	 WHERE  Instrumento."Nemotecnico" IS NULL			
	 GROUP  BY GES_TBCTCIIF."NEMO";	 
--------------------------------------------------------------------------------------------------------------------
------  						Valido Integridad por Garantia
--------------------------------------------------------------------------------------------------------------------
     /*insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
            ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
			,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
			,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
			,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
			
     select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
				, p_IdCarga 					as "IdCarga"
				, 'INTEGRIDAD' 			as "Entidad"
				, 'Garantia_Hom_GT_RF no se carga en HOMOLOGACIÒN datos ya existen o IdCarga no Existe' as "Mensaje"
				, 'COD_PER' 					as "NombreCampoGenerico1"
				, Garantia__Hom."COD_PER" 		as "ValorCampoGenerico1"
				, 'IdGarantia' 					as "NombreCampoGenerico2"
				, Garantia__Hom."IdGarantia"  	as "ValorCampoGenerico2"
				, '' 							as "NombreCampoGenerico3"
				, '' 							as "ValorCampoGenerico3"
				, '' 							as "NombreCampoGenerico4"
				, '' 							as "ValorCampoGenerico4"
				, '' 							as "NombreCampoGenerico5"
				, '' 							as "ValorCampoGenerico5"
				, 'SEC_CAR_INVER' 				as "NombreCampoGenerico6"
				, Garantia__Hom."SEC_CAR_INVER" as "ValorCampoGenerico6"
				, '' 							as "NombreCampoGenerico7"
				, 0 							as "ValorCampoGenerico7"
				, '' 							as "NombreCampoGenerico8"
				, 0 							as "ValorCampoGenerico8"
				, '' 							as "NombreCampoGenerico9"
				, '0001-01-01' 					as "ValorCampoGenerico9"
				, '' 							as "NombreCampoGenerico10"
				,'0001-01-01' 					as "ValorCampoGenerico10"
				
  		FROM 	"BSCL"."BSCL.Homologacion.BD::HOM.Garantia" AS Garantia__Hom
				LEFT JOIN "BSCL"."BSCL.Custodia.BD::GTA.Garantia" AS Garantia__Cus
				ON Garantia__Cus."IdGarantia" =  Garantia__Hom."IdGarantia"
		WHERE 	Garantia__Cus."IdCarga" <> p_IdCarga;*/		  
				  
 -------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------
--SIGNAL SALIDA_CONTROLADA;

-- ENB 2017-6-26 10:30:00

END;
--------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------


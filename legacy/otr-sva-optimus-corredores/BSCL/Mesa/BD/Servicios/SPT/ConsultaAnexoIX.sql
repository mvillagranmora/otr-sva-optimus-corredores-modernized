		
SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.BD.Servicios::ConsultaAnexoIX";

CREATE PROCEDURE 
-- 
--  Proposito: 
--  Fecha Creacion: 08/12/2015
--  
"BSCL.Mesa.BD.Servicios::ConsultaAnexoIX" ( 
	IN p_IdEmpresa      BIGINT,		
	IN p_Fecha          NVARCHAR(10),
	IN p_CodigoComercio BIGINT,   
   	OUT p_out_encabezadoOMA table (
						 "TipoRegistro"		NVARCHAR(2),		
						 "FechaPlanilla"	NVARCHAR(10),
						 "Entidad"          NVARCHAR(3),	
						 "EspacioLibre"     NVARCHAR(200)
						 ),

	--Salida
   	OUT p_out_registrosOMA table (
						 "Registro10"		    BIGINT,		--1
						 "FolioPlanilla"	    BIGINT,		--2	
						 "RutCliente"		    NVARCHAR(9),			--3
						 "DigitoVerificador"    NVARCHAR(1),			--3
						 "Nombre"		        NVARCHAR(50),			--4
						 "TipoDoc"		        BIGINT,			--5
						 "CodigoComercio"	    NVARCHAR(6),			--6
						 "Pais"		            BIGINT,				--7
						 "IdMonedaPrincipal"	NVARCHAR(3),			--8
						 "MontoMonedaPrincipal"	DECIMAL(18,4),		--9
						 "MontoDolar"	        DECIMAL(18,4),		--10
						 "TipoCambio"	        DECIMAL(18,4),		--11
						 "MtoEquiClp"	        DECIMAL(18,4),		--12
						 "AfectoDer"		    BIGINT,			--13
						 "OpZonaFra"		    BIGINT,			--14
						 "TipoAuto"		        NVARCHAR(2),			--15
						 "NumAuto"		        BIGINT,			--16
						 "FecAuto"		        NVARCHAR(8),			--17
						 "CodEntidad"		    BIGINT,			--18
						 "FecPlanilla"          NVARCHAR(8),			--19
						 "NumPlanilla"          BIGINT,			--20		
						 "Registro20"  	        BIGINT,		--1
						 "NroIncrip"	        BIGINT,			--2
						 "NomFinExt"	    NVARCHAR(50),			--3
						 "FecVcto"	        NVARCHAR(8),			--4
						 "FecDese"	        NVARCHAR(8),			--5
						 "MonDese"	        BIGINT,			--6
						 "SecEcoBen"	    BIGINT,			--7
						 "SecEcoInv"	    BIGINT,			--8
						 "PorcPar"	        BIGINT,		--9			   
						 "Registro30"  	    BIGINT,		--1
						 "CodAduExp"	    BIGINT,		--2
						 "FecDecExp"	    NVARCHAR(8),			--3
						 "NroDecExp"	    NVARCHAR(10),			--4
						 "MtoInfor"	        BIGINT,		--5
						 "PlaVcto"	        BIGINT,			--6
						 "NomCompra"	    NVARCHAR(50),			--7 	
						 "Registro40"  	    BIGINT,		--1			
						 "NroDecImp"	    NVARCHAR(18),			--2
						 "FormaPago"	    BIGINT,			--3
						 "FecVctoOp"	    NVARCHAR(8),			--4
						 "MtoInformad"      BIGINT,		--5
						 "NroCuaPago"  	    NVARCHAR(18),			--6						      
						 "Registro50"  	    BIGINT,		--1			
						 "NroConDeri"  	    BIGINT,		--2			
						 "FecSucDeri"  	    NVARCHAR(10),			--3	
						 "Registro60"  	    BIGINT,		--1			
						 "Observacion"      NVARCHAR(198),			--2
						 "Registro99"       BIGINT,		--1			
						 "CantReg10"	    BIGINT,		--2			
						 "CantReg20"	    BIGINT,		--3			
						 "CantReg30"	    BIGINT,		--4			
						 "CantReg40"	    BIGINT,		--5			
						 "CantReg50"	    BIGINT,		--6			
						 "CantReg60"	    BIGINT,		--7
						 "CantRegTot"       BIGINT,		--8						
						 "InformaReg2"      BIGINT,		--1 
						 "InformaReg3"      BIGINT,		--2 
						 "InformaReg4"      BIGINT,		--3 
						 "InformaReg6"      BIGINT		--4 
   	
   	)
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN SEQUENTIAL EXECUTION

		--- Controlado
		DECLARE TipoComprobanteDefecto NVARCHAR(3);
		DECLARE SALIDA_CONTROLADA CONDITION;    	
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
		BEGIN
		END;	
		
		p_out_encabezadoOMA =
		SELECT 
				 '00' as "TipoRegistro"
				,TO_CHAR(p_Fecha,'yyyyMMdd') as "FechaPlanilla"
				,'003' as "Entidad"      
				,'                                                                                                    ' as "EspacioLibre"
		FROM DUMMY;
		
		p_out_registrosOMA =
		Select 
		 10 as "Registro10"
		,"FolioPlanilla"
		,SUBSTR(Cliente."DocumentoIdentidad",0,LENGTH(Cliente."DocumentoIdentidad")-2) as "RutCliente"
		,SUBSTR(Cliente."DocumentoIdentidad",LENGTH(Cliente."DocumentoIdentidad"),LENGTH(Cliente."DocumentoIdentidad")) as "DigitoVerificador"
		,'                                                  '||
		 Cliente."NombreCompleto" as "Nombre"
		,1 as "TipoDoc" --COMPRAVENTA
		,OperacionSpot."CodigoComercio"
		,COALESCE(Cliente."IdPaisNacimiento",997) as "Pais"
		,"IdMonedaPrincipal"
		,"MontoMonedaPrincipal" 
		,CASE WHEN "IdMonedaPrincipal" = "IdMonedaEquivalente" THEN "MontoMonedaPrincipal"
		     WHEN "IdMonedaSecundaria" = "IdMonedaEquivalente" THEN "MontoMonedaSecundaria"
		     ELSE 0 END as "MontoDolar"
		,0 as "TipoCambio"
		,0 as "MtoEquiClp"
		,CASE WHEN "IdTipoCanalIngreso" = 'FWD' Then 1 Else 0 End as "AfectoDer"
		,0 as "OpZonaFra"							--14
		,''	as "TipoAuto"						--15
		,0 as "NumAuto"						--16
		,'' as "FecAuto"						--17
		,0 as "CodEntidad"						--18
		,'' as "FecPlanilla"						--19                                                                                                          
		,0 as "NumPlanilla"						--20
		,0 as "Registro20"  -- Registro 20
		,0 as "NroIncrip"
		,'' as "NomFinExt"
		,'' as "FecVcto"
		,'' as "FecDese"
		,0 as "MonDese"
		,0 as "SecEcoBen"
		,0 as "SecEcoInv"
		,0 as "PorcPar"		
		,0 as "Registro30"
		,0 as "CodAduExp"
		,'' as "FecDecExp"
		,'' as "NroDecExp"
		,0 as "MtoInfor"
		,0 as "PlaVcto"
		,''	as "NomCompra"		-- Registro 30	
		,0 as "Registro40"     -- Registro 40
		,'' as "NroDecImp"
		,0 as "FormaPago"
		,'' as "FecVctoOp"
		,0 as "MtoInformad"
		,'' as "NroCuaPago"			-- Registro 40	
		,Case When "IdTipoCanalIngreso" = 'FWD' Then 50 Else 0 End as "Registro50"				-- Registro 50		
		,Case When "IdTipoCanalIngreso" = 'FWD' Then "FolioInterfaz" Else 0 End	as "NroConDeri"-- Registro 50				  
		,0 as  "FecSucDeri" --pendiente forward revisar
		,Case When "Observacion" <>'' Then 60 Else 0 End as "Registro60"				--Registro 60
		,Case When "Observacion" <>'' Then "Observacion" Else '' End as "Observacion"	--Registro 60
		,0 as "Registro99" -- Registro 99
		,0 as "CantReg10"
		,0 as "CantReg20"
		,0 as "CantReg30"
		,0 as "CantReg40"
		,0 as "CantReg50"
		,0 as "CantReg60"
		,0 as "CantRegTot"				-- Registro 99
		,Case When COALESCE(RegistroComercio20."Estado",'INA') = 'ACT' Then 1 Else 0 End as "InformaReg2"
		,Case When COALESCE(RegistroComercio30."Estado",'INA') = 'ACT' Then 1 Else 0 End as "InformaReg3"
		,Case When COALESCE(RegistroComercio40."Estado",'INA') = 'ACT' Then 1 Else 0 End as "InformaReg4"
		,Case When COALESCE(RegistroComercio60."Estado",'INA') = 'ACT' Then 1 Else 0 End as "InformaReg6"
		FROM "BSCL.Mesa.BD::SPT.OperacionSpot" OperacionSpot
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente 
			ON Cliente."IdCliente" = OperacionSpot."IdCliente"
		LEFT JOIN "BSCL.Mesa.BD::SPT.RegistroComercio" RegistroComercio20
			ON RegistroComercio20."CodigoComercio" = 20 AND OperacionSpot."CodigoComercio" = RegistroComercio20."IdRegistro"
		LEFT JOIN "BSCL.Mesa.BD::SPT.RegistroComercio" RegistroComercio30
			ON RegistroComercio30."CodigoComercio" = 30 AND OperacionSpot."CodigoComercio" = RegistroComercio30."IdRegistro"
		LEFT JOIN "BSCL.Mesa.BD::SPT.RegistroComercio" RegistroComercio40
			ON RegistroComercio40."CodigoComercio" = 40 AND OperacionSpot."CodigoComercio" = RegistroComercio40."IdRegistro"
		LEFT JOIN "BSCL.Mesa.BD::SPT.RegistroComercio" RegistroComercio60
			ON RegistroComercio60."CodigoComercio" = 60 AND OperacionSpot."CodigoComercio" = RegistroComercio60."IdRegistro"
		WHERE
			OperacionSpot."OperacionFicticia" = 'N';


		SIGNAL SALIDA_CONTROLADA;		     
END;
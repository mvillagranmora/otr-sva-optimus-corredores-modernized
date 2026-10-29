SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CIR.PactoVigentes";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CIR.PactoVigentes" ( 
    IN p_IdEmpresa				BIGINT,
    IN p_FechaConsulta			NVARCHAR(10),			 --'YYYY-MM-DD'	
    IN p_cliente                TABLE("IdCliente" BIGINT,"IdPortafolio" BIGINT),
    IN p_fechaHoraSistema       NVARCHAR(40),
	IN p_MostrarSubyacentePacto NVARCHAR(1) DEFAULT 'N',
    OUT p_Salida                TABLE(
    
    								"OrdenInterno"							BIGINT,     
									"CodigoPortafolio"						NVARCHAR(40), 
									"IdPortafolio"							BIGINT,
									"Grupo"									NVARCHAR(40),
									"IdClasificacionTipoInstrumento"		NVARCHAR(10),
									"Nemotecnico"							NVARCHAR(40),
									"IdMoneda"								NVARCHAR(3),
									"Cantidad"								DECIMAL(26,8),
									"ValorPresente"							DECIMAL(26,8),
									"FechaVencimiento"						NVARCHAR(10),
									"PrecioCierre"							DECIMAL(26,8),
									"Tasa"									DECIMAL(26,8),
									"IdCliente"								BIGINT,
									"PrecioDecimales"						DECIMAL(26,8),
									"TasaDecimales"							DECIMAL(26,8)
    							)
)


	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		DECLARE v_FechaConsulta 				NVARCHAR(10) := '2300-12-31';
		DECLARE v_fechaHoraSistema              NVARCHAR(40);	
	
		v_FechaConsulta=p_FechaConsulta;
		v_fechaHoraSistema = p_fechaHoraSistema;	
		   
   		v_decimales = SELECT 
		     MF."PrecioDecimales"
		    ,MF."TasaDecimales" 
		    ,MF."IdMoneda"
		    FROM "BSCL.Plataforma.BD::PLA.MonedaFormato" MF 
		    WHERE  MF."IdTipoInstrumento" = 'PAC'
		    AND MF."IdUsoMonedaFormato" = 'GEN'
		    AND MF."IdTipoOperacion" ='';
		   
		IF  TO_VARCHAR(v_FechaConsulta, 'YYYY-MM') < TO_VARCHAR(v_fechaHoraSistema, 'YYYY-MM') THEN	

			p_Salida = 
				SELECT 	
					 30 AS "OrdenInterno"
					,CP."CodigoPortafolio" AS "CodigoPortafolio"
					,CP."IdPortafolio"     AS "IdPortafolio"
					,'PACTO' || ' - ' || OPE."AccionOperacion" AS "Grupo"
					--ESPECIFICOS
					,COALESCE(SUB."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento"
					,CASE 
						WHEN p_MostrarSubyacentePacto = 'S' THEN 
							CASE 
								WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' 
									THEN COALESCE(SUB."NemoSVS",'')
								ELSE COALESCE(SUB."Nemotecnico",'')
							END
						ELSE 
							CASE 
								WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' 
									THEN COALESCE(CAR."NemoSVS",'')
								ELSE COALESCE(CAR."Nemotecnico",'')
							END
					END AS "Nemotecnico"
					,COALESCE(CAR."IdTipoReajuste" ,'')  AS "IdMoneda"
					,CAR."Cantidad" AS "Cantidad" 
					,CASE 
						WHEN p_MostrarSubyacentePacto = 'S' 
							THEN COALESCE(RES."INGMontoAPlazo", 0)
						ELSE COALESCE(CAR."ValorRescate", 0)
					END AS "ValorPresente"
					,TO_VARCHAR (COALESCE(CAR ."FechaVencimientoOperacion",''), 'DD/MM/YYYY')"FechaVencimiento"
					,0 		AS "PrecioCierre"
					,COALESCE( CAR."TasaCompra" ,0) AS "Tasa"
					,CAR."IdCliente"
					,COALESCE(MF."PrecioDecimales", 0) AS "PrecioDecimales"
					,COALESCE(MF."TasaDecimales", 0) AS "TasaDecimales"
				FROM "BSCL.Custodia.BD::HIS.Subyacente" SUB
				INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" CAR ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera" AND SUB."FechaRespaldo" = CAR."FechaRespaldo"
				INNER JOIN :p_cliente AS CLI ON CLI."IdCliente" = CAR."IdCliente" AND CLI."IdPortafolio" = CAR."IdPortafolio" 
				INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" RES ON RES."IdOperacion" = CAR."IdOperacion" AND RES."FolioSubyacente" = SUB."FolioSubyacente"
				INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" CP ON CP."IdPortafolio"=CAR."IdPortafolio"
				INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" OPE ON CAR."IdTipoOperacion" = OPE."IdTipoOperacion" 
				INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLA ON (CLA."IdClasificacionTipoInstrumento"=CAR."IdClasificacionTipoInstrumento")
				LEFT JOIN :v_decimales AS MF ON MF."IdMoneda" = CAR."IdMonedaInstrumento" 
				WHERE CLA."IdEstado"='ACT'				
				AND CAR."FechaRespaldo" = v_FechaConsulta
				AND	CAR."IdTipoOperacion" IN ('COMPAC','VENPAC')
				AND CAR."IdEstado" = 'VIG'
				AND CAR."IdTipoInstrumento" ='PAC';
				
	    ELSE 	    		
				p_Salida =	
				SELECT 	
					 30 AS "OrdenInterno"
					,CP."CodigoPortafolio" AS "CodigoPortafolio"
					,CP."IdPortafolio"     AS "IdPortafolio"
					,'PACTO' || ' - ' || OPE."AccionOperacion" AS "Grupo"
					--ESPECIFICOS
					,COALESCE(SUB."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento"
					,CASE 
						WHEN p_MostrarSubyacentePacto = 'S' THEN 
							CASE 
								WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' 
									THEN COALESCE(SUB."NemoSVS",'')
								ELSE COALESCE(SUB."Nemotecnico",'')
							END
						ELSE 
							CASE 
								WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' 
									THEN COALESCE(CAR."NemoSVS",'')
								ELSE COALESCE(CAR."Nemotecnico",'')
							END
					END AS "Nemotecnico"
					,COALESCE(CAR."IdTipoReajuste" ,'')  AS "IdMoneda"
					,CAR."Cantidad" AS "Cantidad" 
					,CASE 
						WHEN p_MostrarSubyacentePacto = 'S' 
							THEN COALESCE(RES."INGMontoAPlazo", 0)
						ELSE COALESCE(CAR."ValorRescate", 0)
					END AS "ValorPresente"
					,TO_VARCHAR (COALESCE(CAR ."FechaVencimientoOperacion",''), 'DD/MM/YYYY')"FechaVencimiento"
					,0 		AS "PrecioCierre"
					,COALESCE( CAR."TasaCompra" ,0) AS "Tasa"
					,CAR."IdCliente"
					,COALESCE(MF."PrecioDecimales", 0) AS "PrecioDecimales"
					,COALESCE(MF."TasaDecimales", 0) AS "TasaDecimales"
				FROM "BSCL.Custodia.BD::CUS.Subyacente" SUB
				INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
				INNER JOIN :p_cliente AS CLI ON CLI."IdCliente" = CAR."IdCliente" AND CLI."IdPortafolio" = CAR."IdPortafolio" 
				INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" RES ON RES."IdOperacion" = CAR."IdOperacion" AND RES."FolioSubyacente" = SUB."FolioSubyacente"
				INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" CP ON CP."IdPortafolio"=CAR."IdPortafolio"
				INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" OPE ON CAR."IdTipoOperacion" = OPE."IdTipoOperacion" 
				INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLA ON (CLA."IdClasificacionTipoInstrumento"=CAR."IdClasificacionTipoInstrumento")
				LEFT JOIN :v_decimales AS MF ON MF."IdMoneda" = CAR."IdMonedaInstrumento" 
				WHERE CLA."IdEstado"='ACT'
				AND TO_DATE(CAR."FechaOperacion"  ) <= TO_DATE(:p_FechaConsulta)
				AND	CAR."IdTipoOperacion" IN ('COMPAC','VENPAC')
				AND CAR."IdEstado" = 'VIG'
				AND CAR."IdTipoInstrumento" ='PAC';

			
		END IF;
END;

SET SCHEMA "BSCL"; 
 GO 
DROP PROCEDURE "BSCL.APV.CrystalReports.Servicios::Traspasos.NotificacionNominaTraspasoEgreso"; 
 GO 
CREATE PROCEDURE
--
--  Proposito: Servicio para generar nomina de traspaso egreso
--  Autor: LRIVEROS - PRONETIT
--  Fecha Creacion: 23/07/2019
--
"BSCL.APV.CrystalReports.Servicios::Traspasos.NotificacionNominaTraspasoEgreso" (
	-- Parametros entrada comunes en todos los servicios privados
	IN 	p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN 	p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca
	IN 	p_DocumentoEntidad 		NVARCHAR (10),
	IN	p_DocumentoIdentidad 	NVARCHAR(10),
	IN  p_FechaDesde			DATE,
	IN  p_FechaHasta			DATE
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS

BEGIN
	DECLARE v_Count	 BIGINT:=0;
	DECLARE v_logo TABLE("LogoCorredora" BLOB);
	DECLARE v_firma TABLE("FirmaCorredora" BLOB);
	v_logo = select TOP 1
	"Binario" AS "LogoCorredora"
	from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
	WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
			
	v_tablaSalida = SELECT 
						Traspaso."DocumentoEntidad" AS "RutEntidadDestino"
						,Traspaso."NombreEntidad" AS "NombreEntidadDestino"
						,Traspaso."FechaIngreso"
						,Traspaso."DocumentoIdentidadDefecto"
						,Traspaso."NombreCompleto"
						,Traspaso."IdTraspaso"
						,SolicitudAporte."IdRegimenTributario"
						

						
						,(CASE WHEN SolicitudAporte."IdRegimenTributario"= 'DC-R' THEN 
							(CASE WHEN SolicitudAporte."IdMoneda" <> SolicitudAporte."IdMonedaSistema" THEN 
								CASE WHEN Spot."IdOperacionSpot" IS NOT NULL THEN Spot."MontoMonedaSecundaria"
								ELSE SolicitudAporte."MontoMonedaSistema" 
								END
							ELSE SolicitudAporte."Monto" END) 
						ELSE 0 END) AS MONTODCR
						,(CASE WHEN SolicitudAporte."IdRegimenTributario"= 'DC-AL' THEN 
							(CASE WHEN SolicitudAporte."IdMoneda" <> SolicitudAporte."IdMonedaSistema" THEN 
								CASE WHEN Spot."IdOperacionSpot" IS NOT NULL THEN Spot."MontoMonedaSecundaria"
								ELSE SolicitudAporte."MontoMonedaSistema" 
								END
							ELSE SolicitudAporte."Monto" END) 
						ELSE 0 END) AS MONTODCAL
						,(CASE WHEN SolicitudAporte."IdRegimenTributario"= 'DC-DL'    THEN 
							(CASE WHEN SolicitudAporte."IdMoneda" <> SolicitudAporte."IdMonedaSistema" THEN 
								CASE WHEN Spot."IdOperacionSpot" IS NOT NULL THEN Spot."MontoMonedaSecundaria"
								ELSE SolicitudAporte."MontoMonedaSistema" 
								END
							ELSE SolicitudAporte."Monto" END) 
						ELSE 0 END) AS MONTODCDL
						,(CASE WHEN SolicitudAporte."IdRegimenTributario"= 'APV-A' THEN 
							(CASE WHEN SolicitudAporte."IdMoneda" <> SolicitudAporte."IdMonedaSistema" THEN 
								CASE WHEN Spot."IdOperacionSpot" IS NOT NULL THEN Spot."MontoMonedaSecundaria"
								ELSE SolicitudAporte."MontoMonedaSistema" 
								END
							ELSE SolicitudAporte."Monto" END) 
						ELSE 0 END) AS MONTOAPVA
						,(CASE WHEN SolicitudAporte."IdRegimenTributario"= 'APV-AN' THEN 
							(CASE WHEN SolicitudAporte."IdMoneda" <> SolicitudAporte."IdMonedaSistema" THEN 
								CASE WHEN Spot."IdOperacionSpot" IS NOT NULL THEN Spot."MontoMonedaSecundaria"
								ELSE SolicitudAporte."MontoMonedaSistema" 
								END
							ELSE SolicitudAporte."Monto" END) 
						ELSE 0 END) AS MONTOAPVAN
						,(CASE WHEN SolicitudAporte."IdRegimenTributario"= 'APV-B' THEN 
							(CASE WHEN SolicitudAporte."IdMoneda" <> SolicitudAporte."IdMonedaSistema" THEN 
								CASE WHEN Spot."IdOperacionSpot" IS NOT NULL THEN Spot."MontoMonedaSecundaria"
								ELSE SolicitudAporte."MontoMonedaSistema" 
								END
							ELSE SolicitudAporte."Monto" END) 
						ELSE 0 END) AS MONTOAPVB
						,(CASE WHEN SolicitudAporte."IdRegimenTributario"= 'BF' THEN 
							(CASE WHEN SolicitudAporte."IdMoneda" <> SolicitudAporte."IdMonedaSistema" THEN 
								CASE WHEN Spot."IdOperacionSpot" IS NOT NULL THEN Spot."MontoMonedaSecundaria"
								ELSE SolicitudAporte."MontoMonedaSistema" 
								END
							ELSE SolicitudAporte."Monto" END) 
						ELSE 0 END) AS MONTOBONF
						,(CASE WHEN SolicitudAporte."IdMoneda" <> SolicitudAporte."IdMonedaSistema" THEN 
							CASE WHEN Spot."IdOperacionSpot" IS NOT NULL THEN Spot."MontoMonedaSecundaria"
							ELSE SolicitudAporte."MontoMonedaSistema" 
							END
						ELSE SolicitudAporte."Monto" END) AS "Monto"
					
						
						FROM "BSCL.APV.BD.Vistas::TRA.ConsultarTraspasosAPV" AS Traspaso
						INNER JOIN "BSCL.APV.BD::TRA.OrdenTraspaso" AS OrdenTraspaso 
							 ON OrdenTraspaso."IdTraspaso" =Traspaso."IdTraspaso"
						INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SolicitudAporte 
							 ON SolicitudAporte."IdSolicitudAporteRescate" = OrdenTraspaso."IdSolicitudAporteRescate"
							 AND  SolicitudAporte."Estado"= 'CRT'
						LEFT OUTER  JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" AS Spot ON (Spot."IdOperacionSpotOrigen" = SolicitudAporte."IdOperacionSpot" AND Spot."Estado" != 'MOD' AND Spot."IdOperacionSpotOrigen" != 0)
						WHERE Traspaso."IdEstado"='COM'
						AND Traspaso."TipoMovimiento"='EGRESO'
						AND Traspaso."IdEmpresa"= :p_IdEmpresa
						AND Traspaso."DocumentoEntidad" = CASE WHEN :p_DocumentoEntidad IS NULL THEN Traspaso."DocumentoEntidad" ELSE :p_DocumentoEntidad END
						AND Traspaso."DocumentoIdentidadDefecto" = CASE WHEN IFNULL(:p_DocumentoIdentidad,'') <> '' THEN :p_DocumentoIdentidad ELSE Traspaso."DocumentoIdentidadDefecto" END
						AND Traspaso."FechaIngreso" BETWEEN :p_FechaDesde AND :p_FechaHasta;
						
	SELECT COUNT(1) INTO v_Count FROM :v_tablaSalida;
	
	IF v_Count = 0 THEN
        SELECT	''  AS  "RutEntidadDestino", 
				''  AS  "NombreEntidadDestino", 
				''  AS  "FechaIngreso",
				''  AS  "DocumentoIdentidadDefecto",
				''  AS  "NombreCompleto",
				0  AS  "IdTraspaso",
				''  AS  "IdRegimenTributario",
				0   AS  MONTODCR,
				0   AS  MONTODCAL,
				0   AS  MONTODCDL,
				0   AS  MONTOAPVA,
				0   AS  MONTOAPVAN,
				0   AS  MONTOAPVB,
				0   AS  MONTOBONF,
				0   AS  "Monto"
		FROM 	DUMMY
		CROSS JOIN :v_logo;
	ELSE 

		SELECT * FROM :v_tablaSalida
		CROSS JOIN :v_logo;
	END IF;
END;

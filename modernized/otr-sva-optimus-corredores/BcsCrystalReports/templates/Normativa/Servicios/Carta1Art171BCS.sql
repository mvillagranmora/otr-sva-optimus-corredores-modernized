SET SCHEMA "BSCL"; 
GO 
DROP PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::Carta1Art171BCS"; 
GO

 
CREATE PROCEDURE

"BSCL.Normativa.CrystalReports.Servicios::Carta1Art171BCS" (
	-- Parametros entrada comunes en todos los servicios privados
  	IN p_IdEmpresa			BIGINT,

    --VARIABLES DEL PROCESO
	IN	p_FechaOperacionDesde					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN	p_FechaOperacionHasta					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN	p_IdCliente 							BIGINT 			DEFAULT '0'
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	
		--------------------
		--Variables
		--------------------
		DECLARE v_Count	 BIGINT:=0;
		DECLARE v_FechaSistemaUTC   	NVARCHAR(40);
		DECLARE v_FechaSistemaLOCAL   	NVARCHAR(40);
		DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
		DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
		DECLARE v_FechaHoraUTC   		NVARCHAR(40);
		DECLARE v_FechaHoraLOCAL   		NVARCHAR(40);
		DECLARE v_MensajeError		NVARCHAR(100) := '';

		
		
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
		p_IdEmpresa					=> p_IdEmpresa,
		p_out_FechaSistemaUTC		=> v_FechaSistemaUTC,
		p_out_FechaSistemaLOCAL		=> v_FechaSistemaLOCAL,
		p_out_FechaHoraSistemaUTC	=> v_FechaHoraSistemaUTC,
		p_out_FechaHoraSistemaLOCAL	=> v_FechaHoraSistemaLOCAL,
		p_out_FechaHoraUTC			=> v_FechaHoraUTC,
		p_out_FechaHoraLOCAL		=> v_FechaHoraLOCAL
		);
		
		v_logo = select TOP 1
		"Binario" AS "LogoCorredora"
		from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;

		v_datosGenerales = select 
		"RazonSocial" AS "RazonCorredora", 
		"Direccion"   AS "DireccionCorredora", 
		"RUT" AS "RutCorredora", 
		"Telefono" AS "TelefonoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"
		WHERE "IdEmpresa" = p_IdEmpresa;
		
		

	
		
		CALL "BSCL.Normativa.BD.Servicios.ART171::ConsultarCartaUno"(
			p_IdEmpresa=>:p_IdEmpresa,
			p_FechaOperacionDesde=>:p_FechaOperacionDesde,
			p_FechaOperacionHasta=>:p_FechaOperacionHasta,
			p_IdCliente=>:p_IdCliente,
			p_out_TablaCarta1=>v_Datos_Origen
		);	
	
	--Calcula la paridad para las operaciones que no son en CLP								
	v_tablaSalida=SELECT 
				  * 
				  FROM (
				  	SELECT
				  	RANK() OVER  (PARTITION  BY   DO."IdOperacion" ORDER  BY  DO."IdOperacion", P."Fecha"  DESC ) as RANK
				  	,CASE WHEN DO."IdMoneda"!='CLP' THEN 
							DO."Monto"*P."Valor"
						ELSE
							DO."Monto"
					 END AS "Monto" 
					,TO_VARCHAR(DO."FechaOperacion",'DD-MM-YYYY') AS "FechaOperacion"
				  	,DO."NombreDestinatario",DO."CargoDestinatario",DO."IdOperacion",DO."IdTipoOperacion",DO."NombreTipoOperacion",DO."IdClasificacionTipoInstrumento" 
					,DO."IdInstrumento",DO."Nemotecnico",DO."Cantidad",DO."Precio",DO."IdMoneda",DO."Moneda",DO."IdCliente",DO."NombreCliente",DO."DocumentoIdentidad",DO."TipoDocumentoIdentidad" 
					,DO."NombreTipoDocumentoIdentidad",DO."IdTipoRelacion",DO."NombreTipoRelacion",DO."CodigoRelacionNorma",DO."DescripcionRelacionNorma"				
				  	FROM  :v_Datos_Origen AS DO
				  	LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" AS P ON ( P."Fecha" <=DO."FechaOperacion" AND P."IdParametroFinanciero" = CASE WHEN DO."IdMoneda"='USD' THEN 'US$' ELSE DO."IdMoneda" END )
				  
				  )
				  WHERE RANK = 1;
				 
		
	SELECT COUNT(1) INTO v_Count FROM :v_tablaSalida;

	IF v_Count = 0 THEN
		
		SELECT 
			 ''  AS "NombreDestinatario"
			,''  AS "CargoDestinatario"
			,'0001-01-01'  AS "FechaOperacion"
			,0   AS "IdOperacion"
			,''  AS "IdTipoOperacion"
			,''  AS "NombreTipoOperacion"
			,''  AS "IdClasificacionTipoInstrumento"
			,0   AS "IdInstrumento"
			,''  AS "Nemotecnico"
			,0   AS "Cantidad"
			,0   AS "Precio"
			,0   AS "Monto"
			,''  AS "IdMoneda"
			,''  AS "Moneda"
			,0   AS "IdCliente"
			,''  AS "NombreCliente"
			,''  AS "DocumentoIdentidad"
			,''  AS "TipoDocumentoIdentidad"
			,''  AS "NombreTipoDocumentoIdentidad"
			,''  AS "IdTipoRelacion"
			,''  AS "NombreTipoRelacion"
			,0   AS "CodigoRelacionNorma"
			,''  AS "DescripcionRelacionNorma"
			,''  AS "RazonCorredora"
			,''  AS "DireccionCorredora"
			,''  AS "RutCorredora"
			,''  AS "TelefonoCorredora"
			,''  AS "LogoCorredora"
			,'0001-01-01'  AS "FechaSistemaLOCAL"
			,'00:00:00'  AS "HoraSistemaLOCAL"
			,''  AS "MensajeError"
		FROM DUMMY;
	ELSE 
		SELECT *
			,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
			,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"
			,v_MensajeError	AS "MensajeError"
	    FROM :v_tablaSalida
		CROSS JOIN :v_datosGenerales
		CROSS JOIN :v_logo
		ORDER BY "FechaOperacion" , "Nemotecnico","IdOperacion";	
	END IF;

END;



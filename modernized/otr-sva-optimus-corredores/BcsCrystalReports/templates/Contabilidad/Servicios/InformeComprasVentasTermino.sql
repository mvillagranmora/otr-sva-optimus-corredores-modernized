SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Contabilidad.CrystalReports.Servicios::InformeComprasVentasTermino";

--  Proposito: Compras y Ventas a Término Detallado
--  Fecha Creacion: 01/07/2025

CREATE PROCEDURE "BSCL.Contabilidad.CrystalReports.Servicios::InformeComprasVentasTermino"(
	--Entrada
	IN p_IdEmpresa      BIGINT,
	IN p_FechaBusqueda  NVARCHAR(10) DEFAULT '0001-01-01',
	IN p_IdMesaOrigen   BIGINT,
	IN p_IdPortafolio   BIGINT
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DECLARE varExistenDatos		INT;
	DECLARE v_FechaSistemaUTC    NVARCHAR(40);
	DECLARE v_FechaSistemaLOCAL       NVARCHAR(40);
	DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
	DECLARE v_FechaSistema       SECONDDATE;
	DECLARE v_FechaHoraUTC     NVARCHAR(40);
	DECLARE v_FechaHoraLOCAL     NVARCHAR(40);
	
	-- recupero fecha sistema
  	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
		p_IdEmpresa					=> 1,
		p_out_FechaSistemaUTC		=> v_FechaSistemaUTC,
		p_out_FechaSistemaLOCAL		=> v_FechaSistemaLOCAL,
		p_out_FechaHoraSistemaUTC	=> v_FechaHoraSistemaUTC,
		p_out_FechaHoraSistemaLOCAL	=> v_FechaSistema,
		p_out_FechaHoraUTC			=> v_FechaHoraUTC,
		p_out_FechaHoraLOCAL		=> v_FechaHoraLOCAL
	);
	
	v_logo =
		SELECT TOP 1 "Binario" AS "LogoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR_LOGIN' ORDER BY "Version" DESC;

	v_DatosCorredora =
		SELECT COR1."RazonSocial" ,COR1."RUT" ,COR1."Direccion" , COR1."Telefono" ,COR1."Email" ,COR3."CodigoPortafolio" 
		FROM  "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" COR1 
		inner JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" COR2 ON COR2."DocumentoIdentidad" = COR1."RUT" 
		LEFT JOIN  "BSCL.Portafolio.BD::POR.Portafolio" AS COR3 ON COR3."IdPortafolio" = :p_IdPortafolio
	    WHERE COR1."IdEmpresa" = :p_IdEmpresa;

	v_OperacionesTermino =
		SELECT 
		 OP."IdCliente" 
		,OP."IdMesaOrigen" 
		,OP."IdPortafolio" 
		,OP."IdTipoProducto" 
		,TO_VARCHAR(OP."FechaOperacion",'DD-MM-YYYY') AS "FechaOperacion"
		,OP."FolioOperacion"
		,TO_VARCHAR(RES."FechaVencimiento",'DD-MM-YYYY')AS "FechaVencimiento"
		,OP."IdEstado"
    	,CASE WHEN OP."IdTipoOperacion" = 'VENTER' OR OP."IdTipoOperacion" = 'VENPAC' THEN 'V'
     	      WHEN OP."IdTipoOperacion" = 'COMTER' OR OP."IdTipoOperacion" = 'COMPAC' THEN 'C'
     	      ELSE OP."IdTipoOperacion"
	     END AS "TipoOperacion"
		,RES."Nemotecnico"
		,RES."IdEmisor" 
        ,CASE WHEN RES."IdClasificacionTipoInstrumento" = 'IF'THEN RES."ValorRescateInicial" 
			  ELSE RES."CantidadInicial" END AS "CantidadInicial"	
		,RES."Tasa" 		
		,RES."FechaCreacion" 
		,RES."MontoValorizado"
		,RES."FolioCarteraCustodia" 
		,RES."IdTipoInstrumento"
		,RES."IdTipoReajuste"
        ,TOPE."IdClasificacionTipoInstrumento"
        ,TOPE."Accion" 
        ,TOR."Nombre" AS "NombreTipoOperacion" 
		,CLI."NombreCompleto" 
		,CLI."DocumentoIdentidad"  
		,PORT."CodigoPortafolio" 
		,CASE WHEN OP."IdTipoOperacion" = 'VENTER' THEN COALESCE(CAR."EGRSpread", 0.0) ELSE 0 END AS "Spread"
		,TEM."CodigoTipoEmisor" --familia
		,CASE WHEN TES."Nombre" IS NOT NULL THEN  REPLACE(TES."Nombre", '(Centralizacion) ', '')
          		ELSE  'Otras Empresas'
      	 END AS  "NombreFamilia"
		,CORD."RazonSocial" 
		,CORD."RUT" 
		,CORD."Direccion" 
		,CORD."Telefono" 
		,CORD."Email" 
		,CORD."CodigoPortafolio" AS "PortCorredora"
		,LOGO."LogoCorredora"
	    ,TO_VARCHAR(:v_FechaHoraLOCAL,'HH24:MI')	AS "HoraImpresion"
        ,TO_VARCHAR(:v_FechaHoraLOCAL,'DD-MM-YYYY')	AS "FechaImpresion"
		FROM BSCL."BSCL.Mesa.BD::MES.Operacion" OP 
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI 
			ON OP."IdCliente" = CLI."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT 
			ON PORT."IdPortafolio" = OP."IdPortafolio"
		LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperacionesReserva" RES 
			ON	OP."IdOperacion" = RES."IdOperacion"
		LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR 
			ON RES."IdCarteraOrigen" = CAR."IdCartera"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TOR 
			ON OP."IdTipoProducto" = TOR."IdTipoProducto" 
			AND OP."IdTipoOperacion" = TOR."IdTipoOperacion"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" TOPE	
			ON (OP."IdTipoOperacion" = TOPE."IdTipoOperacion" 
			AND RES."IdTipoInstrumento" = TOPE."IdTipoInstrumento")
		LEFT JOIN (SELECT "IdTipoInstrumento","CodigoTipoEmisor","Nemotecnico" 
		   			FROM "BSCL.Plataforma.BD::MER.TipoInstrumentoTipoEmisor" 
		   			GROUP BY "IdTipoInstrumento","CodigoTipoEmisor","Nemotecnico") AS TEM 
		  	ON TEM."IdTipoInstrumento" = RES."IdTipoInstrumento"
		   	AND TEM."Nemotecnico"= CASE WHEN RES."IdClasificacionTipoInstrumento" = 'IF' AND RES."Nemotecnico" IS NOT NULL 
		                               	THEN RES."Nemotecnico" ELSE '' END
		LEFT JOIN "BSCL.Plataforma.BD::MER.TipoEmisor" TES 
			ON TES."Codigo" = TEM."CodigoTipoEmisor"
		CROSS JOIN :v_DatosCorredora AS CORD
		CROSS JOIN :v_logo AS LOGO 
		WHERE OP."IdTipoProducto"  = 'MERF'
		AND OP."IdTipoOperacion" IN ('COMTER','VENTER' )
		AND OP."IdEstado" NOT IN ( 'ANU', 'PEN', 'LIQ')
		AND OP."FechaOperacion" = :p_FechaBusqueda
		AND OP."IdMesaOrigen" = :p_IdMesaOrigen;
		
				
	    SELECT count (1)INTO varExistenDatos FROM :v_OperacionesTermino;
	   
	  IF :varExistenDatos = 0 THEN
	  v_Dummy =
			SELECT 
			 0  AS "IdCliente" 
		    ,0  AS "IdMesaOrigen" 
			,0  AS "IdPortafolio" 
			,'' AS "IdTipoProducto" 
			,'' AS "FechaOperacion"
			,'' AS "FolioOperacion"
			,'' AS "FechaVencimiento"
			,'' AS "IdEstado"
			,'' AS "IdTipoOperacion"
			,'' AS "Nemotecnico"
			,'' AS "IdEmisor" 
			,'' AS "CantidadInicial"
			,0  AS "Tasa" 		
			,'' AS "FechaCreacion" 
			,0  AS "MontoValorizado"
			,'' AS "FolioCarteraCustodia" 
			,'' AS "IdTipoInstrumento"
			,'' AS "IdTipoReajuste"
			,'' AS "IdClasificacionTipoInstrumento"
			,'' AS "Accion" 
			,'' AS "NombreTipoOperacion" 
			,'' AS "NombreCompleto" 
			,'' AS "DocumentoIdentidad"  
			,'' AS "CodigoPortafolio" 
			,0  AS "Spread"
			,'' AS "CodigoTipoEmisor" --familia
			,'' AS "NombreFamilia"
			,CORD."RazonSocial" 
			,CORD."RUT" 
			,CORD."Direccion" 
			,CORD."Telefono" 
			,CORD."Email" 
			,CORD."CodigoPortafolio" AS "PortCorredora"
			,LOGO."LogoCorredora"
			,TO_VARCHAR(:v_FechaHoraLOCAL,'HH24:MI')	AS "HoraImpresion"
            ,TO_VARCHAR(:v_FechaHoraLOCAL,'DD-MM-YYYY')	AS "FechaImpresion"
			FROM DUMMY
			CROSS JOIN :v_DatosCorredora AS CORD
			CROSS JOIN :v_logo AS LOGO ;
		END IF;
	--Salida del SP	
	IF :varExistenDatos > 0 THEN
		SELECT * FROM :v_OperacionesTermino ;
	ELSE
		SELECT * FROM :v_Dummy ;
	END IF;

END;

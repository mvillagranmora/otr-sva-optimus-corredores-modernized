
SELECT
	 PRO."NumeroReferencia"
    ,TIM."IdTipoMovimiento" AS "IdTipoMovimiento_Real"
    ,CON."IdConcepto" AS "IdConcepto_Real"
    ,MON."IdMoneda" AS "IdMoneda_Real"    
    ,MON."IdEstado" AS "IdEstadoMoneda_Real"
    ,CAJ."IdCaja" AS "IdCaja_Real"
    ,MOT."IdMoneda" AS "IdMonedaTesoreria_Real"
	,CDL."IdCondicionDeLiquidacion" AS "IdCondicionLiquidacion_Real"	
	,TPR."IdTipoProducto" AS "IdTipoProducto_Real"
	,TIO."IdTipoOperacion" AS "IdTipoOperacion_Real"
	,SEC."CountSec"
	,TIM."Nombre"
	,CC."Nombre" AS "NombreConcepto"
	,CEMP."Nombre" AS "Corredora"	
	,CCDG."Telefono" AS "TelefonoCorredora"
	--------------,ARG."MontoTotalFP" ----------------- falta .. borrar .....
	--------------,ARG."ExisteDiferenciaMoneda"	

    -- Datos persona
	,PERNOM."NombreCompleto" AS "NombrePersona"
	,DI."IdTipoDocumentoIdentidad" AS "IdTipoDocumentoIdentidad"
	,DI."DocumentoIdentidad"
	,DI."DocumentoIdentidad" AS "DI"
	,DI."IdTipoDocumentoIdentidad" AS "TipoDoc"
	
    ,PER."IdPersona" AS "IdPersona_Real"
    ,PERESTADO."IdEstado" AS "IdEstado_PersonaReal"
    ,CPT."Numero" AS "NumeroTel"	
    ,CPGDEG."Direccion" AS "Correo"
    
	
	-- Datos titular pago
	,PERTITNOM."NombreCompleto" AS "NombrePersonaTitularPago"
	,IDENT."IdTipoDocumentoIdentidad" AS "IdTipoDocumentoIdentidadTitularPago"
	,IDENT."DocumentoIdentidad" AS "DocumentoIdentidadTitularPago"
    ,PERTIT."IdPersona" AS "IdPersonaRelacionada_Real"
    ,PERTITESTADO."IdEstado" AS "IdEstado_RelacionadoReal"
	
	

	,COALESCE(INDCUMP."ValorIndicador",'N') AS "BloqueadoPorCumplimiento"
	,CON."GeneraComprobanteAutomaticamente"
	,P."IdEstado" AS "EstadoPortafolio"
	,P."CodigoPortafolio"
	,PTIT."IdEstado" AS "EstadoPortafolioTitularPago"
	,PTIT."CodigoPortafolio" AS "CodigoPortafolioTitularPago"
	
 FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso" AS PRO
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" CEMP ON CEMP."IdEmpresa" = :IdEmpresa
 LEFT JOIN "BSCL.Plataforma.BD::CTT.ContratoDatosGenerales"CCDG ON CCDG."IdEmpresa" = :IdEmpresa
 LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" AS TIM ON (TIM."IdTipoMovimiento" = PRO."IdTipoMovimiento")
 LEFT JOIN "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" AS CON ON (CON."IdConcepto" = PRO."IdConcepto")
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CC ON (CC."IdConcepto" = PRO."IdConcepto")
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON (MON."IdMoneda" = PRO."IdMoneda")
 LEFT JOIN "BSCL.Tesoreria.BD::TES.Caja" AS CAJ ON (CAJ."IdCaja" = PRO."IdCaja")
 LEFT JOIN "BSCL.Tesoreria.BD::TES.MonedaTesoreria" AS MOT ON (MOT."IdMoneda" = PRO."IdMoneda" AND MOT."IdEstado" = 'ACT')
 LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = PRO."IdPersona")
 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaEstado" PERESTADO ON PERESTADO."IdPersona" = PER."IdPersona"
 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PERNOM ON PERNOM."IdPersona" = PER."IdPersona"
 LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" CPGDEG ON CPGDEG."IdDireccionElectronica" = PER."IdDireccionElectronicaPrincipal" AND "Predeterminado" = 'S'
 LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERTIT ON (PERTIT."IdPersona" = PRO."IdPersonaTitularPago")
 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaEstado" PERTITESTADO ON PERTITESTADO."IdPersona" = PERTIT."IdPersona"
 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PERTITNOM ON PERTITNOM."IdPersona" = PERTIT."IdPersona"
 LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PERTIT."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
 LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CDL ON (CDL."IdCondicionDeLiquidacion" = PRO."IdCondicionLiquidacion")
 LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS TPR ON (TPR."IdTipoProducto" = PRO."IdTipoProducto")
 LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIO ON (TIO."IdTipoOperacion" = PRO."IdTipoOperacion")
 LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
 LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" CPT ON CPT."IdPersona"=PER."IdPersona" AND PER."IdTelefonoPrincipal" = CPT."IdTelefono" AND CPT."Borrado" = 'N'
 LEFT JOIN ( SELECT "SecuenciaAgrupacion", COUNT("SecuenciaAgrupacion") as "CountSec" 
	 		 FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" 
	 		 GROUP BY "SecuenciaAgrupacion"
 		   ) AS SEC ON (SEC."SecuenciaAgrupacion" = PRO."SecuenciaAgrupacion")



  LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdPersona" = PER."IdPersona")
  LEFT JOIN 
  ( 
  	SELECT IND."IdEntidad", IND."ValorIndicador"
	FROM "BSCL.Plataforma.BD::IND.TipoIndicador" AS TIND
	LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS IND ON  (IND."IdTipoIndicador" = TIND."IdTipoIndicador" AND IND."IdTipoEntidad" = TIND."IdTipoEntidad")
	WHERE TIND."IdTipoEntidad" 		= 'CLIENTE' 
	AND TIND."CodigoTipoIndicador" 	= 'BCCUM' -- Bloqueo por cumplimiento
	AND TIND."IdEstado" 			= 'ACT'
  ) AS INDCUMP ON (INDCUMP."IdEntidad" = CLI."IdCliente")
 LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" P ON P."IdPortafolio" = PRO."IdPortafolio"
 LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" PTIT ON PTIT."IdPortafolio" = PRO."IdPortafolioTitularPago";
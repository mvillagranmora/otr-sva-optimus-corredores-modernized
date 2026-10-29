WITH CUENTAPROP AS (
SELECT  "IdCliente" FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" GROUP BY "IdPersona","IdCliente", "DocumentoIdentidad"
)
SELECT              
    EJE."IdEmpleadoEjecutivo" AS "IdEjecutivo"
   ,PER."NombreCompleto" AS "NombreEjecutivo"
   ,SUC."CodigoSucursal"
   ,SUC."NombreSucursal"
   ,CLI."IdPersona"
   ,CTAINV."IdCuentaInversion"
   ,CTAINV."IdCliente"
   ,CLI."DocumentoIdentidad"
   ,CLI."NombreCompleto" AS "NombreCliente"
   ,CTAINV."IdPortafolio"
   ,POR."DescripcionPortafolio" as "NombrePortafolio"
   ,POR."Proposito" AS "PropositoPortafolio"
   ,POR."CodigoPortafolio"
   ,CTAINV."IdMoneda"
   ,Mon."Codigo" AS "CodigoMoneda"
   ,(Mon."Codigo" ||' - '|| Mon."Nombre") AS "NombreMoneda"
   ,CTAINV."CuentaInversionPrincipal"
   ,ProductoAgrupador."NombreProductoAgrupador" as "Producto"
   ,CASE WHEN COALESCE(POR."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(POR."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
   ,CASE WHEN COALESCE(POR."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MAT."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
   
	,CAST((MAP(ProductoAgrupador."IdProductoAgrupador",NULL,9999,ProductoAgrupador."IdProductoAgrupador")) AS BIGINT) AS "IdProductoAgrupador"
	,CASE WHEN CTAINV."IdCliente" IN (CTAPROP."IdCliente" ) THEN 'CPRO' -- Cuenta propia
										WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
										ELSE 'TER'                                                  -- Tercero
										END AS "Relacion"		   	
	,CLI."IdTipoCliente" AS "TipoCliente"		
	,COALESCE(EMP."CodigoEmpleado",'') 	AS "CodigoEjecutivo"
FROM "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso" AS CINV 
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CTAINV 
        ON (CTAINV."IdPortafolio" = CINV."IdPortafolio")
       AND (CTAINV."IdMoneda" = CINV."IdMoneda")
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = CTAINV."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI ON CLI."IdCliente" = POR."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MAT ON MAT."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = POR."CodigoPortafolio" AND  POR."IdMatrizAgrupadorAPV" = 0
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon ON Mon."IdMoneda" = CTAINV."IdMoneda"
LEFT JOIN "BSCL.SocioNegocio.BD::EJE.Ejecutivo" EJE ON EJE."IdEmpleadoEjecutivo" = POR."IdEmpleadoEjecutivo"
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP ON EJE."IdEmpleadoEjecutivo" = EMP."IdEmpleado"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON PER."IdPersona" = EMP."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal" SUC ON EMP."IdSucursal" = SUC."IdSucursal" 
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MAT."IdProductoAgrupador"
LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarRelacionCorredora" as REL ON (REL."IdPersonaDestino" = CLI."IdPersona")
LEFT JOIN CUENTAPROP AS CTAPROP ON CTAPROP."IdCliente" = CTAINV."IdCliente"
WHERE CINV."IdTrabajo" = :p_IdTrabajo AND (:p_IdProductoAgrupador IS NULL OR MAP(MAT."IdProductoAgrupador",NULL,0,MAT."IdProductoAgrupador") = :p_IdProductoAgrupador);
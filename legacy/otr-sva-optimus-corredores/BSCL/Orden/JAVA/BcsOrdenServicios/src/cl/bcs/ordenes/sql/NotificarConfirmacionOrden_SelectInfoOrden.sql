/**************************************************************
Proposito   : Script consulta info confirmacion orden
autor       : projo
fecha       : 01/02/2017
***********************************************************/

SELECT
    O."IdOrden"
    ,O."Nemotecnico" AS "NemotecnicoOrden"
    ,CC."DocumentoIdentidad" AS "DocumentoIdentidadCliente"
    ,CC."NombreCompleto" AS "NombreCliente"
    ,CC."IdPersona" AS "IdClientePersona"
    ,O."IdPortafolioCliente"
    ,CP."DescripcionPortafolio" AS "PortafolioCliente" 
    ,CE."NombreCompleto" AS "Agente"
    ,CE."IdPersona" AS "IdEmpleadoPersona"
    ,CE."IdDireccionElectronicaPrincipal" AS "IdCorreoEmpleado"
    ,TO_VARCHAR(O."FechaOperacion", 'DD-MM-YYYY') AS "FechaOrden"
    ,TO_VARCHAR(O."FechaConfirmacionPlazo",'DD-MM-YYYY') AS "FechaPlazoConfirmacionOrden"
    ,CTP."Nombre" AS "Producto"
    ,CTO."Nombre" AS "TipoOperacionOrden"
    ,"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(O."Cantidad",'i',0) AS "CantidadOrden"
    ,CCDG."RazonSocial" AS "Corredora"
    ,CCDG."Telefono" AS "AtencionCliente"
FROM "BSCL.Orden.BD::ORD.Orden" O
    INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP ON CP."IdPortafolio" = O."IdPortafolioCliente"
    INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON CC."IdCliente" = CP."IdCliente"
    INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE ON CE."IdEmpleado" = O."IdEmpleadoDigitador"
    INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto" CTP ON CTP."IdTipoProducto" = O."IdProducto"
    INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" CTO ON CTO."IdTipoOperacion" = O."IdTipoOperacion" AND CTO."IdTipoInstrumento" = O."IdTipoInstrumento"
    INNER JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG ON CCDG."IdEmpresa" = :IdEmpresa
WHERE O."IdEmpresa" = :IdEmpresa AND O."IdOrden" IN (:ListaIdOrden)
--WHERE O."IdEmpresa" = 1 AND O."IdOrden" IN (318259)


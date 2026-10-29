SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarOrdenAnalitica"; 

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarOrdenAnalitica" AS
SELECT
    O."IdEmpresa",
    O."IdOrden",
    CASE WHEN IFNULL(O."FechaRecepcion",'')= '' THEN '0001-01-01 00:00:00' 
        ELSE  
           CASE WHEN  O."FechaRecepcion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
           ELSE TO_CHAR(TO_SECONDDATE(UTCTOLOCAL (TO_TIMESTAMP(o."FechaRecepcion", 'YYYY-MM-DD HH24:MI:SS.FF7'), 'America/Santiago','platform')))
        END 
    END AS "FechaRecepcion",
    CASE WHEN IFNULL(O."FechaIngresoRegistro",'')= '' THEN '0001-01-01 00:00:00' 
        ELSE  
           CASE WHEN  O."FechaIngresoRegistro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
           ELSE TO_CHAR(TO_SECONDDATE(UTCTOLOCAL (TO_TIMESTAMP(o."FechaIngresoRegistro", 'YYYY-MM-DD HH24:MI:SS.FF7'), 'America/Santiago','platform')))
        END 
    END AS "FechaIngresoRegistro",
    
    O."FechaIngresoRegistroCorta",
    CASE WHEN O."IdCliente" = 0 THEN NULL ELSE O."IdCliente" END AS "IdCliente",
    CASE PER."Tipo" WHEN 'J' THEN PER."RazonSocial" ELSE CAST(PER."Nombres" AS NVARCHAR(100)) END AS "Nombres",
    CAST(PER."ApellidoPaterno" AS NVARCHAR(100)) AS "ApellidoPaterno",
    CAST(PER."ApellidoMaterno" AS NVARCHAR(100)) AS "ApellidoMaterno",
    CASE WHEN PER."IdPersona" = 0 THEN NULL ELSE PER."IdPersona" END AS "IdPersona",
    CAST(DOC."DocumentoIdentidad" AS NVARCHAR(40)) AS "DocumentoIdentidad",
    CASE WHEN O."IdPortafolioCliente" = 0 THEN NULL ELSE O."IdPortafolioCliente" END AS "IdPortafolioCliente",
    CAST(P."CodigoPortafolio" AS NVARCHAR(100)) AS "CodigoPortafolio",
    CAST(P."DescripcionPortafolio" AS NVARCHAR(100)) AS "DescripcionPortafolio",
    CAST(P."Proposito" AS NVARCHAR(520)) AS "PropositoPortafolio",
    CASE WHEN O."IdProducto" = '' THEN NULL ELSE O."IdProducto" END AS "IdProducto",
    CAST(TP."Nombre" AS NVARCHAR(100)) AS "NombreProducto",
    CASE WHEN O."IdClasificacionTipoInstrumento" = '' THEN NULL ELSE O."IdClasificacionTipoInstrumento" END AS "IdClasificacionTipoInstrumento",
    CAST(CTI."Nombre" AS NVARCHAR(100)) AS "NombreClasificacionTipoInstrumento",
    CASE WHEN O."IdTipoInstrumento" = '' THEN NULL ELSE O."IdTipoInstrumento" END AS "IdTipoInstrumento",
    CAST(TI."Nombre" AS NVARCHAR(100)) AS "NombreTipoInstrumento",
    CASE WHEN O."IdInstrumento" = 0 THEN NULL ELSE O."IdInstrumento" END AS "IdInstrumento",
    CASE WHEN O."IdClasificacionTipoInstrumento" ='IF' THEN CAST(O."NemotecnicoSVS" AS NVARCHAR(40)) ELSE CAST(O."Nemotecnico" AS NVARCHAR(40)) END AS "Nemotecnico",
    CAST(I."ISIN" AS NVARCHAR(40)) AS "ISIN",
    O."EsFungible",
    CASE WHEN O."IdEmisor" = '' THEN NULL ELSE CAST(O."IdEmisor" AS NVARCHAR(40)) END AS "IdEmisor",
    O."EspecificaBolsa",
    CASE WHEN O."IdBolsa" = '' THEN NULL ELSE O."IdBolsa" END AS "IdBolsa",
    CASE WHEN O."IdBolsaDistribucion" = '' THEN NULL ELSE O."IdBolsaDistribucion" END AS "IdBolsaDistribucion",
    
    
    CASE WHEN IFNULL(O."FechaOferta",'')= '' THEN '0001-01-01 00:00:00' 
        ELSE  
           CASE WHEN  O."FechaOferta" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
           ELSE LEFT(TO_CHAR(ADD_SECONDS (O."FechaOferta", CONFIGURACION."Valor")),19)
        END 
    END AS "FechaOferta",

    O."Cantidad",
    O."ValorRescate",
    O."PrecioOrden",
    O."PlazoSimultanea",
    O."TasaSimultanea",
    O."PlazoVentaCorta",
    O."TasaVentaCorta",
    O."PlazoIF",
    O."TasaRFIF",
    CASE WHEN O."IdTipoPrecio" = '' THEN NULL ELSE O."IdTipoPrecio" END AS "IdTipoPrecio",
    CASE WHEN O."IdTipoTasa" = '' THEN NULL ELSE O."IdTipoTasa" END AS "IdTipoTasa",
    O."PrecioMercado",
    CASE WHEN O."IdTipoReajuste" = '' THEN NULL ELSE O."IdTipoReajuste" END AS "IdTipoReajuste",
    TO_NVARCHAR(O."FechaEmision",'YYYY-MM-DD') as "FechaEmision",
    CASE WHEN O."IdCondicionDeLiquidacion" = '' THEN NULL ELSE O."IdCondicionDeLiquidacion" END AS "IdCondicionDeLiquidacion",
    TO_NVARCHAR(O."FechaLiquidacion",'YYYY-MM-DD') as "FechaLiquidacion",
    CASE WHEN O."IdMonedaLiquidacionOrden" = '' THEN NULL ELSE O."IdMonedaLiquidacionOrden" END AS "IdMonedaLiquidacionOrden", 
    O."MontoLiquidacion",
    CASE WHEN O."IdMonedaLiquidacionOrden" = '' THEN NULL ELSE O."IdMonedaLiquidacionOrden" END AS "IdMonedaLiquidacionCobros",-- se coloca asi debido a que se elimino el campo en tabla de ordenes
    CASE WHEN O."IdMonedaOrden" = '' THEN NULL ELSE O."IdMonedaOrden" END AS "IdMonedaOrden",
    O."MontoOrden",
    CASE WHEN I."IdMoneda" = '' THEN NULL ELSE I."IdMoneda" END AS "IdMonedaLocal",
    O."MontoLocal",
    O."TipoCambio",
    TO_NVARCHAR(O."FechaVencimiento",'YYYY-MM-DD') AS "FechaVencimiento",
    CASE WHEN O."IdTipoVigenciaOrden" = '' THEN NULL ELSE O."IdTipoVigenciaOrden" END AS "IdTipoVigenciaOrden",
    TO_NVARCHAR(O."FechaVigencia",'YYYY-MM-DD') AS "FechaVigencia",
    CASE WHEN IFNULL(O."FechaSolicitudAnulacion",'')= '' THEN '0001-01-01 00:00:00' 
        ELSE  
           CASE WHEN  O."FechaSolicitudAnulacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
           ELSE LEFT(TO_CHAR(ADD_SECONDS (O."FechaSolicitudAnulacion", CONFIGURACION."Valor")),19)
        END 
    END AS "FechaSolicitudAnulacion",
    CASE WHEN IFNULL(O."FechaAnulacion",'')= '' THEN '0001-01-01 00:00:00' 
        ELSE  
           CASE WHEN  O."FechaAnulacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
           ELSE LEFT(TO_CHAR(ADD_SECONDS (O."FechaAnulacion", CONFIGURACION."Valor")),19)
        END 
    END AS "FechaAnulacion",
    
    TO_NVARCHAR(O."FechaAceptacion", 'YYYY-MM-DD HH24:MI:SS') AS "FechaAceptacion",
    TO_NVARCHAR(O."FechaAutorizacion",'YYYY-MM-DD') as "FechaAutorizacion",

    CAST(O."Glosa" AS NVARCHAR(520)) AS "Glosa",
    
    CASE WHEN O."OrdenVigente" = 'S' AND COALESCE(CEAO."IdEstadoAsignacion",'SAS') != 'SAS' THEN 
   		CASE WHEN CEAO."IdEstadoAsignacion" = 'ASC' THEN 
   			'EJE'
   		ELSE 
   			'EJP'
   		END
    ELSE 
    	O."IdEstadoOrden"
    END AS "IdEstadoOrden",
    
    CASE WHEN O."OrdenVigente" = 'S' AND COALESCE(CEAO."IdEstadoAsignacion",'SAS') != 'SAS' THEN 
   		CASE WHEN CEAO."IdEstadoAsignacion" = 'ASC' THEN 
   			'EJECUTADO'
   		ELSE 
   			'EJECUTADO PARCIALMENTE'
   		END
    ELSE 
    	E."Nombre"
    END AS "NombreEstadoOrden",
    
    O."IndicadorFacturacionInmediata",
    CASE WHEN O."IdTipoOperacion" = '' THEN NULL ELSE O."IdTipoOperacion" END AS "IdTipoOperacion",
    CASE WHEN IFNULL(O."FechaOperacion",'')= '' THEN '0001-01-01 00:00:00' 
        ELSE  
           CASE WHEN  O."FechaOperacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
           ELSE TO_CHAR(TO_SECONDDATE(UTCTOLOCAL (TO_TIMESTAMP(o."FechaOperacion", 'YYYY-MM-DD HH24:MI:SS.FF7'), 'America/Santiago','platform')))
        END 
    END AS "FechaOperacion",
    

    CASE WHEN O."IdTipoCanalIngreso" = '' THEN NULL ELSE O."IdTipoCanalIngreso" END AS "IdTipoCanalIngreso",
    CASE WHEN O."IdLibroOrdenes" = 0 THEN NULL ELSE O."IdLibroOrdenes" END AS "IdLibroOrdenes",
    CASE WHEN CEAO."IdEstadoAsignacion" = '' THEN NULL ELSE CEAO."IdEstadoAsignacion" END AS "IdEstadoAsignacion",
    O."IdCarga",
    O."EsVentaCorta",
    O."SecOrdenMigracion",
    CASE WHEN O."IdTipoCanalDistribucion" = '' THEN NULL ELSE O."IdTipoCanalDistribucion" END AS "IdTipoCanalDistribucion",
    CASE WHEN O."IdInstrumentoSubyacente" = 0 THEN NULL ELSE O."IdInstrumentoSubyacente" END AS "IdInstrumentoSubyacente",
    CAST(COALESCE(I2."Nemotecnico",'') AS NVARCHAR(40)) AS "NemotecnicoSubyacente",
    CAST(COALESCE(I2."ISIN",'0') AS NVARCHAR(40)) AS "ISINSubyacente",
    CASE WHEN O."IdPorcentajeVisibilidad" = 0 THEN NULL ELSE O."IdPorcentajeVisibilidad" END AS "IdPorcentajeVisibilidad",
    CASE WHEN O."IdBoveda" = '' THEN NULL ELSE CAST(O."IdBoveda" AS NVARCHAR(40)) END AS "IdTipoCustodia",
    CASE WHEN O."IdFormaCustodia" = '' THEN NULL ELSE O."IdFormaCustodia" END AS "IdFormaCustodia",
    O."Reservado",
    CASE WHEN O."IdUsuarioReserva" = 0 THEN NULL ELSE O."IdUsuarioReserva" END AS "IdUsuarioReserva",
   COALESCE( CAST(CE."NombreCompleto" AS NVARCHAR(302)),'') AS "NombreUsuarioReserva",
    CAST(O."MotivoReserva" AS NVARCHAR(40)) AS "MotivoReserva",
    O."Bloqueado",
    CASE WHEN O."IdUsuarioBloqueo" = 0 THEN NULL ELSE O."IdUsuarioBloqueo" END AS "IdUsuarioBloqueo",
    COALESCE(CAST(CE2."NombreCompleto" AS NVARCHAR(302)),'') AS "NombreUsuarioBloqueo",
    CAST(O."MotivoBloqueo" AS NVARCHAR(40)) AS "MotivoBloqueo",
    CAST(O."Folio" AS NVARCHAR(40)) AS "Folio",
    CASE WHEN O."IdEmpleadoPortafolio" = 0 THEN NULL ELSE O."IdEmpleadoPortafolio" END AS "IdEmpleadoPortafolio",
    COALESCE(CAST(CE3."NombreCompleto" AS NVARCHAR(302)),'')  AS "NombreUsuarioPortafolio",
    CASE WHEN O."IdEmpleadoDigitador" = 0 THEN NULL ELSE O."IdEmpleadoDigitador" END AS "IdEmpleadoDigitador",
    COALESCE(CAST(CE4."NombreCompleto" AS NVARCHAR(302)),'') AS "NombreUsuarioDigitador",
    CASE WHEN O."CodigoClaseOrden" = '' THEN NULL ELSE CAST(O."CodigoClaseOrden" AS NVARCHAR(40)) END AS "CodigoClaseOrden",
    O."CodigoProcedenciaOrden" AS "CodigoProcedenciaOrden",
    IFNULL(FPAGOORD."IdMedioDePago",'CIN') AS "IdMedioDePago",
    CAST(P."ComentarioBloqueo" AS NVARCHAR(520)) AS "ComentarioBloqueo",
    CAST(P."IdTipoBloqueo" AS NVARCHAR(40))  AS "IdTipoBloqueo",
    CAST(P."IdPerfilComercialBaseOrigen" AS NVARCHAR(40))  AS "IdPerfilComercialBaseOrigen",
    CAST(P."IdPerfilProductos" AS NVARCHAR(40))  AS "IdPerfilProductos",
    CAST(P."IdPerfilCobros" AS NVARCHAR(40))  AS "IdPerfilCobros",
    CAST(P."IdPerfilLimites" AS NVARCHAR(40))  AS "IdPerfilLimites",
    CAST(P."IdTipoDesbloqueo" AS NVARCHAR(40))  AS "IdTipoDesbloqueo",
    CAST(P."ComentarioDesbloqueo" AS NVARCHAR(520)) AS "ComentarioDesbloqueo",
    CASE WHEN P."IdTelefonoContacto" = 0 THEN NULL ELSE P."IdTelefonoContacto" END AS "IdTelefonoContacto",
    CASE WHEN P."IdDireccionEnvioCorrespondencia" = 0 THEN NULL ELSE P."IdDireccionEnvioCorrespondencia" END AS "IdDireccionEnvioCorrespondencia",
	CASE WHEN O."IdInstrumentoNegociacion" = 0 THEN NULL ELSE O."IdInstrumentoNegociacion" END AS "IdInstrumentoNegociacion",
	CASE WHEN O."GeneraSpot" = '' THEN NULL ELSE CAST(O."GeneraSpot" AS NVARCHAR(1)) END AS "GeneraSpot"
FROM "BSCL.Orden.BD::ORD.Orden" O
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" P ON P."IdPortafolio" = O."IdPortafolioCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON P."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" TP ON TP."IdTipoProducto" = O."IdProducto"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CTI ON CTI."IdClasificacionTipoInstrumento" = O."IdClasificacionTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TI ON TI."IdTipoInstrumento" = O."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" I ON I."IdInstrumento" = O."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoAnalitica" E ON E."IdEstado" = O."IdEstadoOrden" AND E."NombreEntidad" = 'ORDEN'
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarEstadoAsignacionOrden" CEAO ON CEAO."IdOrden" = O."IdOrden"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" I2 ON I2."IdInstrumento" = O."IdInstrumentoSubyacente"
LEFT JOIN "BSCL.Orden.BD::ORD.FormaDePagoOrden" FPAGOORD ON FPAGOORD."IdOrden" = O."IdOrden"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USURES ON USURES."IdUsuario" = O."IdUsuarioReserva" AND USURES."TipoEntidad" = 'EMP'
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE ON CE."IdEmpleado" = USURES."IdEntidad"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USUBLO ON USUBLO."IdUsuario" = O."IdUsuarioBloqueo" AND USUBLO."TipoEntidad" = 'EMP'
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE2 ON CE2."IdEmpleado" = USUBLO."IdEntidad"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE3 ON CE3."IdEmpleado" = O."IdEmpleadoPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE4 ON CE4."IdEmpleado" = O."IdEmpleadoDigitador"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONFIGURACION ON "CodigoParametro" = 'PLA.SegundosZonaHoraria' ;

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarOrdenAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

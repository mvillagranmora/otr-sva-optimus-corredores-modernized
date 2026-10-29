SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1899.PreDatos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1899.PreDatos" (
    "ID",
    "IdDocumento",
    "VboUsuario",
    "Observaciones",
    "FuenteDatos",
    ----
    "Numero",
    "IdMoneda",
    "RutCliente",
    "IdCliente",
    "ActivoPensionado",
    "RutEmpleador",
    --"IdEmpleador",
    "FechaOperacion",
    "DepositoEmpleador",--uf
    ---Aporte Empleador
    "AporteEmpleador",--uf
    "TraspasoEmpleador",--uf
    "RetiroEmpleadorActualizado",--($)
    "MontoRetencionActualizado",--15%($)
    --Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
    "AhorrosModalidadIndirectaEmpleador",
    "AhorrosModalidadDirectaTrabajador",
    --DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
    --del ART. 42 Nª1 de la LIR en UF
    "CotizacionesModalidadIndirectaEmpleador",--uf
    "CotizacionesModalidadDirectaTrabajador",--uf
    --DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
    --del Art.42 Nª2 de la LIR en UF
    "CotizacionesTrabajadorIndependiente",--uf
    --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
    --del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
    --los aportes del empleador, actualizados en $
    "MontoRetiroPorAhorrosIncisoPrimero",
    --Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
    "RetencionImpuestoSobreRetiros",--15%($)
    --Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
    "MontoRetiroPorAhorrosIncisoSegundo",
    --N° CERTIFICADO TRABAJADOR
    "NumeroCertificadoTrabajador",
    --N° CERTIFICADO EMPLEADOR
    "NumeroCertificadoEmpleador"
) AS
SELECT
    d."ID",
    d."IdDocumento",
    d."VboUsuario",
    COALESCE(VAL."CodigoValidacion", d."Observaciones") AS "Observaciones",
    d."FuenteDatos",
    ----
    d."Numero",
    d."IdMoneda",
    d."RutCliente",
    d."IdCliente",
    d."ActivoPensionado",
    d."RutEmpleador",
    --d."IdEmpleador",
    d."FechaOperacion",
    COALESCE(d."DepositoEmpleadorMonedaDJ",d."DepositoEmpleador") AS "DepositoEmpleador",--uf
    ---Aporte Empleador
    COALESCE(d."AporteEmpleadorMonedaDJ",d."AporteEmpleador") AS "AporteEmpleador",--uf
    COALESCE(d."TraspasoEmpleadorMonedaDJ",d."TraspasoEmpleador") AS "TraspasoEmpleador",--uf
    COALESCE(d."RetiroEmpleadorActualizadoIPC",d."RetiroEmpleadorActualizadoMonedaDJ",d."RetiroEmpleadorActualizado") AS "RetiroEmpleadorActualizado",--($)
    COALESCE(d."MontoRetencionActualizadoIPC",d."MontoRetencionActualizadoMonedaDJ",d."MontoRetencionActualizado") AS "MontoRetencionActualizado",--15%($)
    --Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
    COALESCE(d."AhorrosModalidadIndirectaEmpleadorMonedaDJ",d."AhorrosModalidadIndirectaEmpleador") AS "AhorrosModalidadIndirectaEmpleador",
    COALESCE(d."AhorrosModalidadDirectaTrabajadorMonedaDJ",d."AhorrosModalidadDirectaTrabajador") AS "AhorrosModalidadDirectaTrabajador",
    --DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
    --del ART. 42 Nª1 de la LIR en UF
    COALESCE(d."CotizacionesModalidadIndirectaEmpleadorMonedaDJ",d."CotizacionesModalidadIndirectaEmpleador") AS "CotizacionesModalidadIndirectaEmpleador",--uf
    COALESCE(d."CotizacionesModalidadDirectaTrabajadorMonedaDJ",d."CotizacionesModalidadDirectaTrabajador") AS "CotizacionesModalidadDirectaTrabajador",--uf
    --DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
    --del Art.42 Nª2 de la LIR en UF
    COALESCE(d."CotizacionesTrabajadorIndependienteMonedaDJ",d."CotizacionesTrabajadorIndependiente") AS "CotizacionesTrabajadorIndependiente",--uf
    --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
    --del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
    --los aportes del empleador, actualizados en $
    COALESCE(d."MontoRetiroPorAhorrosIncisoPrimeroIPC",d."MontoRetiroPorAhorrosIncisoPrimeroMonedaDJ",d."MontoRetiroPorAhorrosIncisoPrimero") AS "MontoRetiroPorAhorrosIncisoPrimero",
    --Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
    COALESCE(d."RetencionImpuestoSobreRetirosIPC",d."RetencionImpuestoSobreRetirosMonedaDJ",d."RetencionImpuestoSobreRetiros") AS "RetencionImpuestoSobreRetiros",--15%($)
    --Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
    COALESCE(d."MontoRetiroPorAhorrosIncisoSegundoMonedaDJ",d."MontoRetiroPorAhorrosIncisoSegundo") AS "MontoRetiroPorAhorrosIncisoSegundo",
    --N° CERTIFICADO TRABAJADOR
    d."NumeroCertificadoTrabajador",
    --N° CERTIFICADO EMPLEADOR
    d."NumeroCertificadoEmpleador"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1899" d
LEFT JOIN (
           	SELECT "CodigoValidacion", "IdDocumento" ,"RutCliente","IdCliente", "ActivoPensionado","RutEmpleador","FechaOperacion","VboUsuario"
			FROM (
			    SELECT 
			        R."CodigoValidacion", 
			        DT."IdDocumento",
			        DT."RutCliente", 
			        DT."IdCliente",
			        DT."ActivoPensionado",
					DT."RutEmpleador",
					DT."FechaOperacion",
					DT."VboUsuario",
			        ROW_NUMBER() OVER (
			            PARTITION BY DT."IdCliente" 
			            ORDER BY R."ID" ASC
			        ) AS rn
			    FROM "BSCL.Normativa.BD::TRI.ResumenValidacion" AS R
			    INNER JOIN "BSCL.Normativa.BD::TRI.DatosDJ1899" AS DT ON DT."IdDocumento" = R."IdDocumento"
			    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
			) AS t
			WHERE t.rn = 1
        ) AS VAL ON  d."IdDocumento" = VAL."IdDocumento" AND d."VboUsuario" = VAL."VboUsuario" AND d."RutCliente" = VAL."RutCliente" 
        	AND d."ActivoPensionado" = VAL."ActivoPensionado" AND d."RutEmpleador" = VAL."RutEmpleador" AND d."FechaOperacion" = VAL."FechaOperacion"
with read only;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1899.PreDatos" to bcs_desarrollador with grant option;
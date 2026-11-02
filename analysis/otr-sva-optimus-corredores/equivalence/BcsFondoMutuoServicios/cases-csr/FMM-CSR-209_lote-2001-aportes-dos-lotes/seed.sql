-- case seed on top of seed/base-fmm.sql + seed/base-csr.sql (fake data)
CREATE VIEW "CSR.ConteoSolicitudes" AS SELECT COUNT(*) AS "Filas", MAX("IdSolicitudAporteRescate") AS "UltimoId", MIN("IdSolicitudAporteRescate") AS "PrimerId" FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" WHERE "IdTipoMovimientoFondoMutuo" = 'APOREINV';

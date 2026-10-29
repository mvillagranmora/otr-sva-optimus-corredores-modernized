-- H2-only emulation of HANA session features used by the in-scope SQL (never executed against HANA).
-- HANA functions: implemented in cl.bcs.oracle.H2Fn (runner classes).
CREATE ALIAS ADD_SECONDS FOR "cl.bcs.oracle.H2Fn.addSeconds";
CREATE ALIAS TO_BIGINT FOR "cl.bcs.oracle.H2Fn.toBigint";
CREATE ALIAS TO_NVARCHAR FOR "cl.bcs.oracle.H2Fn.toNvarchar";
-- SELECT ... FROM DUMMY (FechaDB: SELECT CURRENT_UTCTIMESTAMP FROM DUMMY)
CREATE TABLE DUMMY (DUMMY VARCHAR(1));
INSERT INTO DUMMY VALUES ('X');
-- Session local table of CrearSolicitudAporteRescateNormal_CreateTableBloqueoSolicitudAporteRescate.sql (DDL text of that SQL: types are REAL, taken from it).
-- HANA: CREATE LOCAL TEMPORARY TABLE #p_Bloqueo... / DROP TABLE #p_Bloqueo... per request. H2: this table always exists (a real H2 DDL would implicitly
-- COMMIT the pending writes of the request; whether HANA does that is OPEN); cl.bcs.oracle.SessionConnection tracks "created/dropped" per session and
-- the dump reports a table left without DROP (tablasLocalesSinDrop). One session per case, so a shared H2 table behaves as a session local one.
CREATE TABLE "#p_BloqueoSolicitudAporteRescate" (
  "IdBloqueoSolicitudAporteRescate" NVARCHAR(120),
  "IdPortafolio" BIGINT,
  "IdSerieFondo" NVARCHAR(40),
  "IdRegimenTributario" NVARCHAR(40),
  "TipoSolicitud" NVARCHAR(3),
  "Monto" DECIMAL(26,8),
  "NumeroCuotas" DECIMAL(26,8),
  "FechaSistema" DATE
);

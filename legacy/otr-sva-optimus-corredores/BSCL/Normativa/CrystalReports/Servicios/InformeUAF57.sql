SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::InformeUAF57";

CREATE PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::InformeUAF57"(
 -- Entrada
 IN p_IdEmpresa BIGINT,
 IN p_IdPersona BIGINT DEFAULT 0
 )
 LANGUAGE SQLSCRIPT
 SQL SECURITY INVOKER
 DEFAULT SCHEMA "BSCL"
 AS
 BEGIN
 DECLARE varFechaHoraSistema NVARCHAR(40);
 DECLARE v_logo TABLE("LogoCorredora" BLOB);
 DECLARE v_count   BIGINT := 0;
 DECLARE v_CountErrores BIGINT := 0;
 v_logo =
 SELECT TOP 1 "Binario" AS "LogoCorredora"
 FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
 WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
 CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
 p_IdEmpresa => :p_IdEmpresa,
 p_out_FechaHoraSistema => :varFechaHoraSistema
 );
 varFechaHoraSistema = TO_CHAR(:varFechaHoraSistema,'yyyy-MM-dd')||' 00:00:00';
 v_RegistrosInformeUAF57 =
 SELECT
 COALESCE(INDFOLUAF."ValorIndicador",'0') AS "FolioComprobante"
 ,PER."DocumentoIdentidad"     AS "RutEntidad"
 ,PER."NombreCompleto"      AS "NombreEntidad"
 ,REL."NombreCompleto"      AS "NombreCompleto"
 ,REL."IdTipoRelacion"
 ,REL."NombreTipoRelacion"
 ,REL."PorcentajeParticipacion"
 ,COALESCE(TO_CHAR(REL."FechaModificacion",'DD-MM-YYYY'),'01-01-0001') AS "FechaUltimaActualizacion"
 ,LOGO."LogoCorredora"
 ,:v_CountErrores       AS "ErroresContador"
 ,''           AS "MensajeError"
 ,CASE WHEN IND."ValorIndicador" = 'S' THEN 'CONTRATO PERMANENTE' ELSE 'CONTRATO NO PERMANENTE' END AS "GrupoContratoPermanente"
 ,PER."DocumentoIdentidad"||' '||PER."NombreCompleto" AS "GrupoDatosEntidad"
 FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
 INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" INDUAF
 ON (PER."IdPersona" = INDUAF."IdPersona" AND
 INDUAF."CodigoTipoIndicador" = 'UAF57' AND
 INDUAF."ValorIndicador" = 'S' AND
 INDUAF."IdEmpresa" = :p_IdEmpresa)
 LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" INDFOLUAF
 ON (PER."IdPersona" = INDFOLUAF."IdPersona" AND
 INDFOLUAF."CodigoTipoIndicador" = 'FOLIOUAF57' AND
 INDFOLUAF."IdEmpresa" = :p_IdEmpresa)
 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" REL
 ON (PER."IdPersona" = REL."IdPersonaOrigen" AND
 REL."IdTipoRelacion" IN ('AGYRLE','BENFIN','CONEFE','PERDEC','ALTGER','RELEEN'))
 LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND
 ON (PER."IdPersona" = IND."IdPersona" AND
 IND."CodigoTipoIndicador" = 'CCG' AND
 IND."IdEmpresa" = :p_IdEmpresa)
 LEFT JOIN :v_logo LOGO
 ON (LOGO."LogoCorredora" IS NOT NULL)
 WHERE (PER."IdPersona" = :p_IdPersona OR :p_IdPersona = 0)
 AND PER."Tipo" = 'J'
 AND REL."Administracion" = 'RELACIONADOS'
 AND REL."IdEstado" = 'ACT'
 AND REL."IdEmpresa" = :p_IdEmpresa;
 SELECT COUNT(*) INTO v_count FROM :v_RegistrosInformeUAF57;
 IF (:v_count = 0) THEN
 v_CountErrores := v_CountErrores + 1;
 v_RegistrosInformeUAF57 =
 SELECT
 0 AS "FolioComprobante"
 ,'' AS "RutEntidad"
 ,'' AS "NombreEntidad"
 ,'' AS "NombreCompleto"
 ,'' AS "IdTipoRelacion"
 ,'' AS "NombreTipoRelacion"
 ,0 AS "PorcentajeParticipacion"
 ,'01-01-0001' AS "FechaUltimaActualizacion"
 ,LOGO."LogoCorredora"
 ,:v_CountErrores     AS "ErroresContador"
 ,'No se encontraron operaciones.' AS "MensajeError"
 ,'' AS "GrupoContratoPermanente"
 ,'' AS "GrupoDatosEntidad"
 FROM DUMMY
 INNER JOIN :v_logo LOGO
 ON (LOGO."LogoCorredora" IS NOT NULL);
 END IF;
 --Salida del SP
 SELECT * FROM :v_RegistrosInformeUAF57;
 END;

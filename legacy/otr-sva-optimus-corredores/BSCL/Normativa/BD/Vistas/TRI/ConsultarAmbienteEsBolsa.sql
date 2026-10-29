SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarAmbienteEsBolsa";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarAmbienteEsBolsa" (
"IdEmpresa"
,"CodigoCorredora"
,"IdBolsa"
)
AS 
SELECT CORREDORA."IdEmpresa", CORREDORA."CodigoCorredora", CORREDORA."IdBolsa"
from "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" AS CORREDORA
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad" AS DOC ON DOC."IdPersona" = CORREDORA."IdPersona" 
WHERE  CORREDORA."IdBolsa"='BCS'
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.ConsultarAmbienteEsBolsa" TO bcs_desarrollador WITH GRANT OPTION;
SET SCHEMA "BSCL"; 
--GO 
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaTelefono";
--GO
create view "BSCL.Homologacion.BD.Vistas::HomologaTelefono" as
SELECT
	TEL."IdTelefono" AS tel_id, --int] NOT NULL,
	PER."IdPersona" AS per_id, --int] NOT NULL, *********
	CASE WHEN TEL."IdTipoTelefono" = 'FIJO' then 'TTELEFIJO' ELSE  TEL."IdTipoTelefono" END AS par_tte_id, --varchar](10) NOT NULL, ********
	TEL."Numero" AS tel_numero, --varchar](20) NOT NULL, ************
	'' AS tel_observacion, --varchar](50) NOT NULL,
	CASE WHEN TEL."IdEstado" = 'ACT' then 'N' ELSE  'S' END AS tel_invalido, --char](1) NOT NULL, *************
	TEL."FechaModificacion" AS tel_fec_modifica, --datetime] NOT NULL,
	'' AS tel_modificable, --char](1) NOT NULL,
	'' AS tel_com_id --varchar](10) NOT NULL,
from "BSCL.SocioNegocio.BD::PER.Persona" as PER
INNER JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS TEL ON PER."IdPersona" = TEL."IdPersona"
;
--GO
GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaTelefono" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
--GOahi conec
SET SCHEMA "BSCL"; 
--GO 
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaCuentaBancaria";
--GO
create view "BSCL.Homologacion.BD.Vistas::HomologaCuentaBancaria" as
SELECT
	CTA."IdCuentaBancaria" AS cba_id, --int] NOT NULL,
	PER."IdPersona" AS per_id, --int] NOT NULL,*********
	'' AS mon_id, --char](3) NOT NULL,
	CTA."IdBanco" AS ban_id, --varchar](4) NOT NULL,***********
	'' AS par_tcu_id, --varchar](10) NOT NULL,
	'' AS pai_iso, --char](2) NOT NULL,
	'' AS cba_sucursal, --varchar](30) NOT NULL,
	CTA."NumeroCuentaBancaria" AS cba_cuenta, --varchar](20) NOT NULL,*************
	'' AS cba_beneficiario, --varchar](50) NOT NULL,
	'' AS cba_ind_ppal, --char](1) NOT NULL,
	'' AS cba_ind_beneficiario, --char](1) NOT NULL,
	'' AS cta_ben_tip_doc, --varchar](10) NOT NULL,
	'' AS cta_ben_num_doc, --varchar](20) NOT NULL,
	'' AS cba_ind_default, --char](1) NOT NULL,
	'' AS cba_ord_permanente, --char](1) NOT NULL,
	'' AS cba_grupo, --varchar](20) NOT NULL,
	'' AS cba_depositoEnEstaCta --char](1) NOT NULL,
from "BSCL.SocioNegocio.BD::PER.Persona" as PER
INNER JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CTA ON PER."IdPersona" = CTA."IdPersona"
;
--GO
GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaCuentaBancaria" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
--GOahi conec
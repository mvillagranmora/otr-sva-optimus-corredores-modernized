SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.OPCIONES_ENTREGA_MAILING";
CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.OPCIONES_ENTREGA_MAILING" AS

Select
	CAST(
		CASE 
			WHEN instr(IDENT."DocumentoIdentidad" , '-') = 0 THEN -1 
			ELSE trim(LEFT(IDENT."DocumentoIdentidad"  , instr(IDENT."DocumentoIdentidad" , '-')-1)) END AS INT) AS perid,	--INT
	POR."CodigoPortafolio" AS CUENTA,						--INT
	0 as NOTIFICACION_DIARIA,								--INT
	COALESCE(ELE."IdDireccionElectronica",-1) as dir_id_primario,		--INT
	-1 as dir_id_secundario,									--INT
	'' as suspencion_inicio,								--VARCHAR(10)
	'' as suspencion_fin,									--VARCHAR(10)
	0 as suspencion											--INT
FROM "BSCL.SocioNegocio.BD::PER.Persona" as PER
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON CLI."IdPersona" = PER."IdPersona"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio"  as POR ON CLI."IdCliente" = POR."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" ELE ON PER."IdPersona" = ELE."IdPersona" AND ELE."Predeterminado" = 'S' and ELE."IdEstado" = 'ACT'
WHERE "BSCL.Replica.BD.Servicios::func_IsNumeric"(POR."CodigoPortafolio") != 0
and IDENT."IdDocumentoIdentidad" IS NOT NULL
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.OPCIONES_ENTREGA_MAILING" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

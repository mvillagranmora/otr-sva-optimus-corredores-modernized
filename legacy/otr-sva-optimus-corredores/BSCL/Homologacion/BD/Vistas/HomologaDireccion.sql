SET SCHEMA "BSCL"; 
--GO 
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaDireccion";
--GO
create view "BSCL.Homologacion.BD.Vistas::HomologaDireccion" as
SELECT
	DF."IdDireccionFisica" AS dir_id,--int] NOT NULL,
	DP."IdDivisionPais" AS com_id,--int] NOT NULL, ***
	'TDIREF' AS par_tdi_id,--varchar](10) NOT NULL,***
	DF."IdPersona" AS per_id,--int] NOT NULL,***
	DF."Calle" AS dir_nombre,--varchar](50) NOT NULL,***
	DF."Numero" AS dir_numero,--varchar](15) NOT NULL,***
	DF."Departamento"  AS dir_complemento,--varchar](50) NOT NULL,***
	DF."CodigoPostal" AS dir_cod_postal,--varchar](11) NOT NULL,
	' ' AS dir_presentacion,--varchar](80) NOT NULL,***
	' ' AS dir_invalida,--char](1) NOT NULL,
	DF."FechaModificacion" AS dir_fec_modifica,--datetime] NOT NULL,
	' ' AS dir_barrio,--varchar](30) NOT NULL,
	' ' AS dir_contacto,--varchar](50) NOT NULL,
	' ' AS dir_modificable,--char](1) NOT NULL,
	' ' AS dir_ind_principal--char](1) NOT NULL,
FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" DF
JOIN "BSCL.Plataforma.BD::REG.DivisionPais" DP ON DF."IdCiudad" = DP."IdCiudad" AND DF."IdDivisionPais" = DP."IdDivisionPais"
UNION ALL
SELECT
	DF."IdDireccionElectronica" AS dir_id,--int] NOT NULL,
	0 AS com_id,--int] NOT NULL, ***
	'TDIREE' AS par_tdi_id,--varchar](10) NOT NULL,***
	DF."IdPersona" AS per_id,--int] NOT NULL,***
	' ' AS dir_nombre,--varchar](50) NOT NULL,***
	' ' AS dir_numero,--varchar](15) NOT NULL,***
	' '  AS dir_complemento,--varchar](50) NOT NULL,***
	' ' AS dir_cod_postal,--varchar](11) NOT NULL,
	DF."Direccion" AS dir_presentacion,--varchar](80) NOT NULL,***
	' ' AS dir_invalida,--char](1) NOT NULL,
	DF."FechaModificacion" AS dir_fec_modifica,--datetime] NOT NULL,
	' ' AS dir_barrio,--varchar](30) NOT NULL,
	' ' AS dir_contacto,--varchar](50) NOT NULL,
	' ' AS dir_modificable,--char](1) NOT NULL,
	' ' AS dir_ind_principal--char](1) NOT NULL,
FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DF
;
--GO
GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaDireccion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
--GOahi conec
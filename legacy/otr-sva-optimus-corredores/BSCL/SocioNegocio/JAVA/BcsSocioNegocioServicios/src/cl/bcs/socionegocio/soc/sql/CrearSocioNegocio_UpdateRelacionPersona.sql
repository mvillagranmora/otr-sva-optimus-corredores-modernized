UPDATE
	"BSCL.SocioNegocio.BD::PER.Persona"
SET
	"IdPersonaConyuge" = IFNULL(:p_IdPersonaConyuge,"IdPersonaConyuge")
   ,"IdPersonaEmpleador" = IFNULL(:p_IdPersonaEmpleador,"IdPersonaEmpleador")
   ,"IdPersonaRepresentante" = IFNULL(:p_IdPersonaRepresentante,"IdPersonaRepresentante")
WHERE
	"IdPersona" = :p_IdPersona;
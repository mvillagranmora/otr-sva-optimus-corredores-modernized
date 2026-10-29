SELECT 
	BO."IdBoveda"														AS "IdBoveda",
	'CNT'																AS "TipoSocioNegocio",					
	IFNULL(BO."DocumentoIdentidad",'')									AS "DocumentoIdentidad",				
	IFNULL(BO."Nombre",'')												AS "NombreCliente"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda" AS BO
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SN ON (BO."IdPersona" = SN."IdPersona")
WHERE   
	BO."IdBoveda" = :IdBoveda AND 
	SN."IdEmpresa" = :IdEmpresa
UPDATE SPT
SET
	 SPT."IdUsuario" = DAT."IdUsuarioCreador"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
INNER JOIN "BSCL.Mesa.BD::SPT.OperacionSpotDatatec" DAT
	ON (SPT."IdOperacionSpot" = DAT."IdOperacionSpot" AND
		SPT."IdDatatec" = DAT."IdDatatec" AND
		SPT."IdEmpresa" = DAT."IdEmpresa")
WHERE DAT."IdUnicoDatatec" IN (:IdUnicoDatatec);
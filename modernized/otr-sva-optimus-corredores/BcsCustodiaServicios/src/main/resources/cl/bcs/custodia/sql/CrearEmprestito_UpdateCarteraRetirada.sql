UPDATE
	"BSCL.Custodia.BD::CUS.Subyacente"
SET 
	"IdCarteraRetirada" = :IdCarteraRetirada
WHERE
	"IdSubyacente" = :IdSubyacente AND "FolioSubyacente" = :FolioSubyacente;

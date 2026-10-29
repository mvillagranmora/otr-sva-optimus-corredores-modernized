UPDATE "BSCL.Orden.BD::ORD.Orden"
SET  "IdTipoCanalDistribucion" = 'MAN',
	"IdBolsaDistribucion" = :IdBolsa
    ,"FechaOferta" = :FechaOferta
WHERE "IdOrden" = :IdOrden
INSERT INTO "BSCL.Custodia.BD::HIS.FusionCarteraDetalle"
("IdCartera", "IdFusion", "Cantidad", "Tasa", "ValorRescate" , "CorteMinimo", "EsFungible")
VALUES
(:IdCartera, 
(SELECT Idfustable."IdFusion" FROM "BSCL.Custodia.BD::HIS.FusionCartera" Idfustable WHERE Idfustable."IdCarteraResultante" = :FolioCustodia) ,
:Cantidad , :Tasa , :ValorRescate , :CorteMinimo , :EsFungible);
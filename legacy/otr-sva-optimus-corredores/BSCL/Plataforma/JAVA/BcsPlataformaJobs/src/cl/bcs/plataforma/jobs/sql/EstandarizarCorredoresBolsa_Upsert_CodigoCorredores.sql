UPSERT "BSCL.Plataforma.BD::INS.CorredoraCodigos"("IdCorredora", "IdBolsa", "CodigoCorredora", "CodigoCorredoraFix")
VALUES(:IdCorredora, :IdBolsa, :CodigoCorredora, LPAD (:CodigoCorredora, 3, '0') )
WHERE "IdCorredora" = :IdCorredora AND "IdBolsa" = :IdBolsa;
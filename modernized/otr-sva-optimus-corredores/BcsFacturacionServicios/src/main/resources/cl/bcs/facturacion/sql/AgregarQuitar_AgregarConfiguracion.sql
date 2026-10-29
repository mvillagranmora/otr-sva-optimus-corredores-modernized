INSERT INTO "BSCL.Facturacion.BD::FAC.ConfiguracionAFP"
("IdConfiguracionAFP", "IdEmpresa", "IdPortafolio", "IdCliente", "IdAFP", "IdMultifondo", "FechaIngreso")
VALUES("BSCL.Facturacion.BD.Secuencias::FAC.IdConfiguracionAFP".NEXTVAL, :IdEmpresa, :IdPortafolio, :IdCliente, :IdAFP, :IdMultifondo, :FechaIngreso);

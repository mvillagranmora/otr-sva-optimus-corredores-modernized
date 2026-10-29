INSERT INTO "BSCL.Plataforma.BD::INS.PresenciaBursatil"
("IdPresenciaBursatil",
 "IdInstrumento",
 "Nemotecnico",
 "FechaPresencia",
 "Factor"
 )
VALUES(
 "BSCL.Plataforma.BD.Secuencias::PresenciaBursatil".NEXTVAL,
 :IdInstrumento,
 :Nemotecnico,
 :FechaVigencia,
 :Factor);
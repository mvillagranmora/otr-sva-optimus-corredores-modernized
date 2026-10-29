INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBCPDTPC', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCPDTPC" WHERE cod_per = '2022-01';

--SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCPDTPC";
--SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCPDTPC";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBCPMSPC', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCPMSPC" WHERE cod_per = '2022-01';
--SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCPMSPC";
--SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCPMSPC";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBCTCIFM', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIFM" WHERE cod_per = '2022-01';
--SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIFM" WHERE cod_per = '2022-01';

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBCTCIIF', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIIF" WHERE cod_per = '2022-01';
--SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIIF";
--SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIIF";
--sec car inver repetido 70554

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBCTCIRF', 'I', 0,  CURRENT_TIMESTAMP , ''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRF" WHERE cod_per >= '2021-04';
--SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRF"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBCTCIRV', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRV" WHERE cod_per >= '2022-01';
--SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRV";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBCTCTCL', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCTCL" WHERE cod_per = '2022-02';
--SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCTCL"
--SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCTCL"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBCTCUFM', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCUFM" WHERE cod_per = '2022-06';

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBCTCURF', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCURF" WHERE cod_per = '2022-06';
--SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCURF";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBCTCURV', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCURV" WHERE cod_per = '2022-08';
--SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCURV";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBCTGTIA', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTGTIA" WHERE cod_per >= '2022-01';

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBCTTRCO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTTRCO";
--SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTTRCO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBCTTRTP', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTTRTP" WHERE cod_per >= '2021-04';
--SELECT  COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTTRTP";
--SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTTRTP";

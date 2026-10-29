select * from "BSCL.Replica.BD.Vistas::Sebra.FM_INSTRUMENTOS" brbvsfi 

SELECT * FROM "2022_05_31_FM_INSTRUMENTOS"
where CODIGO_INS  not in ( select CODIGO_INS from "BSCL.Replica.BD.Vistas::Sebra.FM_INSTRUMENTOS" fi)
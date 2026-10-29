SELECT * FROM TBCTCIIF WHERE COD_PER = '2021-04'

SELECT * FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTCIIF"

--bovedas estan malas
--cantidad de registros
SELECT *
FROM TBCTCIIF SEBRA
LEFT JOIN "BSCL.Replica.BD.Vistas::Sebra.TBCTCIIF" OPT ON SEBRA.RUT_CLI = OPT.RUT_CLI 
AND SEBRA.SEC_RUT_CLI = OPT.SEC_RUT_CLI AND TRIM(sebra.EMISOR) = OPT.emisor 
AND sebra.TIPO_REAJ = opT.TIPO_REAJ AND SEBRA.FEC_VCTO = opt.FEC_VCTO
AND SEBRA.NEMO = OPT.NEMO AND sebra.VAL_RESC = opt.val_resc AND sebra.SEC_CAR_INVER = opt.sec_car_inver
AND sebra.VAL_RESC_PLAZO = opt.val_resc_plazo
WHERE opt.rut_cli IS null
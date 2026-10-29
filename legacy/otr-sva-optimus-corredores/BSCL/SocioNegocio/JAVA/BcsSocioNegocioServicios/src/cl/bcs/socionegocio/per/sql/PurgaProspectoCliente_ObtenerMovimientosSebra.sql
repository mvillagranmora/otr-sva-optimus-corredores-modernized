SELECT LTRIM(RTRIM(TBCTMVTO.RUT_CLI)) AS RutTitular
FROM TBCTMVTO
WHERE TBCTMVTO.IND_ANULADO <> 'A'
  AND LTRIM(RTRIM(TBCTMVTO.RUT_CLI)) IN (:listaRuts)
  AND DATEDIFF(
        DAY,
        TBCTMVTO.FEC_MVTO,
        CONVERT(
            DATETIME,
            SUBSTRING(:fechaSistema, 1, 4) + '-' +
            SUBSTRING(:fechaSistema, 5, 2) + '-' +
            SUBSTRING(:fechaSistema, 7, 2) + ' ' +
            SUBSTRING(:fechaSistema, 9, 2) + ':' +
            SUBSTRING(:fechaSistema, 11, 2) + ':' +
            SUBSTRING(:fechaSistema, 13, 2) + '.' +
            SUBSTRING(:fechaSistema, 15, 3),
            121
        )
      ) < :diasBorradoDefinitivo;
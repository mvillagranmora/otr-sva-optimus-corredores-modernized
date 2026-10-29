UPSERT "BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad"
("FechaSistema",
"IdCliente",
"IdPortafolio",
"IdMoneda",
"TipoSaldo",
"Saldo")
VALUES(
:FechaSistema,
:IdCliente,
:IdPortafolio,
:IdMoneda,
:TipoSaldo,
:Saldo)
WHERE "FechaSistema" = :FechaSistema AND "IdCliente" = :IdCliente AND "IdPortafolio" = :IdPortafolio 
AND "IdMoneda" = :IdMoneda AND "TipoSaldo" = :TipoSaldo;
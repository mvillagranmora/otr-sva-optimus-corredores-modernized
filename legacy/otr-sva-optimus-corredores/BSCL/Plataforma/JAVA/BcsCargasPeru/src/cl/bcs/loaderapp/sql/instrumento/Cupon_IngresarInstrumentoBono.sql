INSERT INTO "BSCL.Plataforma.BD::INS.Cupon" (
    "IdCupon",
    "Nemotecnico",
    "Numero",
    "Fecha",
    "Call",
    "Intereses",
    "Amortizacion",
    "SaldoPorAmortizar"
)VALUES (
    "BSCL.Plataforma.BD.Secuencias::VAL.Cupon".NEXTVAL,
    :Nemotecnico,
    :Numero_Cupon,
    :Fecha_Pago_Cupon,
    :Indicador_Call,
    :Intereses,
    :Amortizacion,
    :Saldo_amortizar
);
--"BSCL.Plataforma.BD::INS.InstrumentoBono"
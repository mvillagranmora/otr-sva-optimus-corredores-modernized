select SUM("TotalMontoPesos") "SumaMandatoPac" from 
"BSCL.Tesoreria.BD.Vistas::PAC.ConsultarMandatoPAC"
where "EstadoCliente" = 'ACT';  
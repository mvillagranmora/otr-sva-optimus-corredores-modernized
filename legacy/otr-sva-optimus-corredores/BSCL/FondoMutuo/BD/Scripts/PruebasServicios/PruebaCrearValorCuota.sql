set schema bscl;
DROP table #valores;
create local temporary table #valores(
												"IDSERIEFONDO"		NVARCHAR(40),
												"IDMONEDA"			NVARCHAR(3),
												"FECHACIERRE"		NVARCHAR(10),
												"VALORCUOTA"		DECIMAL(20,6),
												"VALORDOLARAPORTE"		DECIMAL(20,6),
    											"VALORDOLARRESCATE"		DECIMAL(20,6),
    											"VALORDOLAROBSERVADO"	DECIMAL(20,6),
    											"OBSERVACION"			NVARCHAR(100),
												"MensajeValidacion" NVARCHAR(3)
);

DELETE FROM  #valores;
INSERT INTO #valores VALUES('FMLIQUIDEZ_A','USD','2016-12-30',5.2556,678.00,678.00,678.00,'HOLA', 'OK');



select * from #valores;



call "BSCL.FondoMutuo.BD.Servicios::CrearValorCuota" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_UsaReglasNegocio => 'N',
	p_ValorCuota => #valores,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);


SELECT * FROM "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico";
SELECT * FROM "BSCL.FondoMutuo.BD::FMM.ValorCuota";

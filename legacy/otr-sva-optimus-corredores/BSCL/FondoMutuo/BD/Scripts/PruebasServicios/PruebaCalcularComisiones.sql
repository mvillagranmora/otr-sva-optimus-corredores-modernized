set schema bscl;
DROP table #datosCustodia;
create local temporary table #datosCustodia(
											 "IdInterno"		    BIGINT,
										  	 "IdSerieFondo" 	    NVARCHAR(40),
										  	 "IdInstrumento"        BIGINT,
										  	 "IdCliente"		    BIGINT,
										  	 "IdPortafolio"	        BIGINT,
										  	 "Cantidad"             DECIMAL(26,8),
										  	 "TipoSolicitud"        NVARCHAR(3)
);

DELETE FROM #datosCustodia;
INSERT INTO #datosCustodia VALUES(0,'FMLIQUIDEZ_A',346394,524891,512348,5000,'RES');

select * from #datosCustodia;

call "BSCL.FondoMutuo.BD.Utilitarios::CalcularComisiones" (
	p_IdEmpresa	 => 1,
	p_IdUsuario => 21,
	p_IdModulo => 'FFM',
	p_SimularOperacion => 'N',
	p_UsaReglasNegocio => 'N',
	p_DatosCustodia => #datosCustodia,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);



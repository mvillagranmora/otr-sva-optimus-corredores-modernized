select *
	 from "BSCL.Custodia.BD.Vistas::ConsultarCustodiaPortafolioAnaliticaHis" WITH PARAMETERS(
		'PLACEHOLDER' = ('$$p_idempresa$$','1'),
		'PLACEHOLDER' = ('$$p_fechaconsulta$$','2020-09-04'),
		'PLACEHOLDER' = ('$$p_tipofechaconsulta$$','ADQ')
);

select *
	 from "BSCL.Custodia.BD.Vistas::ConsultarCustodiaPortafolioAnalitica" WITH PARAMETERS(
		'PLACEHOLDER' = ('$$p_idempresa$$','1'),
		'PLACEHOLDER' = ('$$p_fechaconsulta$$','0001-01-01'),
		'PLACEHOLDER' = ('$$p_tipofechaconsulta$$','ADQ')
);
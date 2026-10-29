INSERT INTO
		    "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" (
		        "IdPreferenciaPagoPortafolio",
		        "IdPortafolio",
		        "IdPreferenciaPago",
		        "IdCuentaBancaria",
		        "IdTarjetaBancaria",
		        "FechaCreacion",
		        "FechaModificacion",
		        "IdUsuarioCreador",
		        "IdEstado",
		        "IdEmpresa",
		        "FechaIngreso",
		        "IdEstadoAutorizacion",
		        "PorDefecto"
		    )
		SELECT
		    "BSCL.Portafolio.BD.Secuencias::PreferenciaPagoPortafolio".NEXTVAL,
		    :IdPortafolio,
		    PRE."IdPreferenciaPago",
		    0,
		    0,
		    CURRENT_UTCTIMESTAMP,
		    --p_Creacion, -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		    CURRENT_UTCTIMESTAMP,
		    p_IdUsuario,
		    'ACT',
		    p_IdEmpresa,
		    v_FechaIngreso,
		    'AUT',
		    'S'
		FROM
		    "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago" PRE
		WHERE
		    PRE."IdMedioDePago" = 'CIN'
		    AND PRE."IdMoneda" = :v_MonedaSistema
		    AND "IdTipoOperacion" IN ('COMTER', 'VENTER');

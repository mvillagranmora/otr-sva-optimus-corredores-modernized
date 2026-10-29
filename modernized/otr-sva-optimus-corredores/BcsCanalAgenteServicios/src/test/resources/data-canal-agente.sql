-- Test data for BcsCanalAgenteServicios integration tests.

-- Portfolio 66720 belongs to client 1234
INSERT INTO PORTAFOLIO_T (ID_PORTAFOLIO, ID_CLIENTE) VALUES (66720, 1234);

-- Portfolio 99999 has no associated client (omitted — tests use this absence)

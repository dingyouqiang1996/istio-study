package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@RestController
public class App 
{
    public static void main( String[] args )
    {
        SpringApplication.run(App.class, args);
    }

    @RequestMapping("/")
    public String getJwks(){
        String jwks=
        "{\"keys\":[{ "+
           " \"kty\": \"RSA\","+
               " \"e\": \"AQAB\","+
               "\"use\": \"sig\","+
               " \"kid\": \"cIcAfQ3B8kj2ucJTDABS7jTqVQ5fjwKQV7whPNLFCW4\","+
                "\"alg\": \"RS256\","+
                "\"n\": \"qggabdCirBa84zr9G1gn4Ha2IJe6hDi4oP2FXOuSznGzXt40vXlHlJGAxlUNasA2dI3QjFtqj79Kxo2uh8-VcNs2Xs5NVv98rDRiE6ZfItBTzIfmhZAZANrLn2TEXWy4iQVBt1OhlR4uWEzMaQRAabOEEFCm-T8WGyrz8qJZFu4rJ5BWSMETVEw1gey-EqV_tJznoTju9t_LEFyLO_PMdUADRgMOCN0pckb8rMLcaV0a1twW1EEkQC15pRI2TwusOm-ooVwekDrTqmILqJdhvHzZiTOc7raVBYizjD8Qjdr_D4lAPLrCosYUkORpfAkVBpZ-MQ8Se4UPy6GnfBoHVQ\""+
        "}]}";
        return jwks;
    }
}

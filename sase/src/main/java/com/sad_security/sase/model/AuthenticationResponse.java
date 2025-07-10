package com.sad_security.sase.model;

// Classe che restituisce, alla richesta di autenticazione, il token o un errore 
public class AuthenticationResponse {
    
    private String jwt;
	private String error;

	public AuthenticationResponse(String jwt, String error) {
		this.jwt = jwt;
		this.error = error;
	}

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
    
}

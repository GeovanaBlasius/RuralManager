package br.com.ruralmanager.usuario;

/**
 * Perfil de acesso do usuario.
 *
 * ADMIN atende a operacao do sistema e nao possui produtor vinculado.
 * PRODUTOR e OPERADOR pertencem sempre a um produtor e enxergam apenas
 * os dados dele.
 */
public enum PerfilUsuario {

    /** Operacao do sistema. Sem produtor vinculado. */
    ADMIN,

    /** O proprio produtor rural. Acesso total aos dados da propriedade. */
    PRODUTOR,

    /** Quem lanca dados em nome do produtor: conjuge, filho, contador. */
    OPERADOR;

    /** Indica se o perfil exige vinculo com um produtor. */
    public boolean exigeProdutor() {
        return this != ADMIN;
    }
}

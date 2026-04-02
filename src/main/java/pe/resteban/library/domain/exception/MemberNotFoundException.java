package pe.resteban.library.domain.exception;

import pe.resteban.library.domain.model.MemberId;

public class MemberNotFoundException extends DomainException {

    public MemberNotFoundException(MemberId id) {
        super("Member not found with id: " + id);
    }

    public MemberNotFoundException(String message) {
        super(message);
    }
}

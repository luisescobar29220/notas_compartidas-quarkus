package repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import model.RoomModel;

@ApplicationScoped
public class RoomRepository implements PanacheRepository<RoomModel> {

    public RoomModel findByCode(String code){
        return find("code",code).firstResult();
    }

    public Boolean existsByCode(String code){
        return count("code",code)>0;
    }

}

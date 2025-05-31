package com.seatwise.common.builder;

import com.seatwise.show.entity.Show;
import com.seatwise.show.entity.ShowType;
import com.seatwise.show.repository.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventTestDataBuilder {

  @Autowired private ShowRepository showRepository;
  private String title;
  private String description;
  private ShowType type;

  public EventTestDataBuilder(ShowRepository showRepository) {
    this.showRepository = showRepository;
  }

  public EventTestDataBuilder withTitle(String title) {
    this.title = title;
    return this;
  }

  public EventTestDataBuilder withDescription(String description) {
    this.description = description;
    return this;
  }

  public EventTestDataBuilder withType(ShowType type) {
    this.type = type;
    return this;
  }

  public Show build() {
    Show show = new Show(title, description, type);
    return showRepository.save(show);
  }
}

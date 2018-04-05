package app;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ManagePersonServlet extends HttpServlet {

	// Идентификатор для сериализации/десериализации.
	private static final long serialVersionUID = 1L;

	// Основной объект, хранящий данные телефонной книги.
	private Phonebook phonebook;

	public ManagePersonServlet() {
		// Вызов родительского конструктора.
		super();

		// Создание экземпляра телефонной книги.
		try {
			this.phonebook = Phonebook.getInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Валидация ФИО и генерация сообщения об ошибке в случае невалидных данных.
	private String validatePersonFMLName(Person person) {
		String error_message = "";

		if (!Person.validateFMLNamePart(person.getName(), false)) {
			error_message += "Имя должно быть строкой от 1 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br/>";
		}

		if (!Person.validateFMLNamePart(person.getSurname(), false)) {
			error_message += "Фамилия должна быть строкой от 1 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br/>";
		}

		if (!Person.validateFMLNamePart(person.getMiddlename(), true)) {
			error_message += "Отчество должно быть строкой от 0 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br/>";
		}
		return error_message;
	}

	// Реакция на GET-запросы.
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Обязательно ДО обращения к любому параметру нужно переключиться в UTF-8,
		// иначе русский язык при передаче GET/POST-параметрами превращается в "кракозябры".
		request.setCharacterEncoding("UTF-8");

		// В JSP нам понадобится сама телефонная книга. Можно создать её экземпляр там,
		// но с архитектурной точки зрения логичнее создать его в сервлете и передать в JSP.
		request.setAttribute("phonebook", this.phonebook);

		// Хранилище параметров для передачи в JSP.
		HashMap<String, String> jsp_parameters = new HashMap<String, String>();

		// Диспетчеры для передачи управления на разные JSP (разные представления (view)).
		RequestDispatcher dispatcher_for_list = request.getRequestDispatcher("/List.jsp");
		RequestDispatcher dispatcher_for_manager = request.getRequestDispatcher("/ManagePerson.jsp");
		RequestDispatcher dispatcher_for_adding_person = request.getRequestDispatcher("/AddPerson.jsp");
		RequestDispatcher dispatcher_for_adding_phone = request.getRequestDispatcher("/AddPhone.jsp");
		RequestDispatcher dispatcher_for_editing_phone = request.getRequestDispatcher("/EditPhone.jsp");

		// Действие (action) и идентификатор записи (id) над которой выполняется это действие.
		String action = request.getParameter("action");
		String id_person = request.getParameter("id");
		String id_phone = request.getParameter("phoneId");

		// Если идентификатор и действие не указаны, мы находимся в состоянии
		// "просто показать список и больше ничего не делать".
		if ((action == null) && (id_person == null)) {
			request.setAttribute("jsp_parameters", jsp_parameters);
			dispatcher_for_list.forward(request, response);
		}
		// Если же действие указано, то...
		else {
			switch (action) {

				// Добавление записи.
        		case "addPerson":
				// Создание новой пустой записи о пользователе.
        		Person empty_person = new Person();

				// Подготовка параметров для JSP.
        		jsp_parameters.put("current_action", "addPerson");
				jsp_parameters.put("next_action", "addPerson_go");
				jsp_parameters.put("next_action_label", "Добавить");

				// Установка параметров JSP.
        		request.setAttribute("person", empty_person);
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
        		dispatcher_for_adding_person.forward(request, response);
				break;

			case "editPerson":
				// Извлечение из телефонной книги информации о редактируемой записи.        			
        		Person editable_person = this.phonebook.getPerson(id_person);

				// Подготовка параметров для JSP.
        		jsp_parameters.put("current_action", "editPerson");
				jsp_parameters.put("next_action", "editPerson_go");
				jsp_parameters.put("next_action_label", "Сохранить");

				// Установка параметров JSP.
        		request.setAttribute("person", editable_person);
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
        		dispatcher_for_manager.forward(request, response);
				break;

			case "deletePerson":
				// Если запись удалось удалить...
				if (phonebook.deletePerson(id_person)) {
					jsp_parameters.put("current_action_result", "DELETION_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Удаление выполнено успешно");
				} else {
					// Если запись не удалось удалить (например, такой записи нет)...
        			jsp_parameters.put("current_action_result", "DELETION_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка удаления (возможно, запись не найдена)");
				}

				// Установка параметров JSP.
        		request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
        		dispatcher_for_list.forward(request, response);
				break;

			case "addPhone":
				// Извлечение из телефонной книги информации о редактируемой записи.        			
        		Person add_phone_person = this.phonebook.getPerson(id_person);

				jsp_parameters.put("current_action", "addPhone");
				jsp_parameters.put("next_action", "addPhone_go");
				jsp_parameters.put("next_action_label", "Добавить номер");

				request.setAttribute("person", add_phone_person);
				request.setAttribute("jsp_parameters", jsp_parameters);
				dispatcher_for_adding_phone.forward(request, response);
				break;

			case "editPhone":
				// Извлечение из телефонной книги информации о редактируемой записи.        			
        		Person edit_phone_person = this.phonebook.getPerson(id_person);

				jsp_parameters.put("current_action", "editPhone");
				jsp_parameters.put("next_action", "editPhone_go");
				jsp_parameters.put("next_action_label", "Сохранить номер");

				request.setAttribute("person", edit_phone_person);
				request.setAttribute("jsp_parameters", jsp_parameters);
				request.setAttribute("phoneId", id_phone);
				dispatcher_for_editing_phone.forward(request, response);
				break;

			case "deletePhone":
				// Извлечение из телефонной книги информации о редактируемой записи.        			
        		Person delete_phone_person = this.phonebook.getPerson(id_person);
				if (phonebook.deletePhone(id_phone, delete_phone_person)) {
					jsp_parameters.put("current_action_result", "DELETION_SUCCESS");
					jsp_parameters.put("next_action_label", "Сохранить");
					jsp_parameters.put("next_action", "editPerson_go");
					jsp_parameters.put("current_action_result_label", "Удаление выполнено успешно");
				} else {
					jsp_parameters.put("current_action_result", "DELETION_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка удаления (возможно, запись не найдена)");
				}

				request.setAttribute("jsp_parameters", jsp_parameters);
				request.setAttribute("person", delete_phone_person);
				dispatcher_for_manager.forward(request, response);
				break;
			}
		}
	}

	// Реакция на POST-запросы.
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Обязательно ДО обращения к любому параметру нужно переключиться в UTF-8,
		// иначе русский язык при передаче GET/POST-параметрами превращается в "кракозябры".
		request.setCharacterEncoding("UTF-8");

		// В JSP нам понадобится сама телефонная книга. Можно создать её экземпляр там,
		// но с архитектурной точки зрения логичнее создать его в сервлете и передать в JSP.
		request.setAttribute("phonebook", this.phonebook);

		// Хранилище параметров для передачи в JSP.
		HashMap<String, String> jsp_parameters = new HashMap<String, String>();

		// Диспетчеры для передачи управления на разные JSP (разные представления (view)).
		RequestDispatcher dispatcher_for_list = request.getRequestDispatcher("/List.jsp");
		RequestDispatcher dispatcher_for_manager = request.getRequestDispatcher("/ManagePerson.jsp");
		RequestDispatcher dispatcher_for_adding_person = request.getRequestDispatcher("/AddPerson.jsp");
		RequestDispatcher dispatcher_for_adding_phone = request.getRequestDispatcher("/AddPhone.jsp");
		RequestDispatcher dispatcher_for_editing_phone = request.getRequestDispatcher("/EditPhone.jsp");

		// Действие (add_go, edit_go) и идентификатор записи (id) над которой выполняется это действие.
		String addPerson_go = request.getParameter("addPerson_go");
		String editPerson_go = request.getParameter("editPerson_go");
		String editPhone_go = request.getParameter("editPhone_go");
		String addPhone_go = request.getParameter("addPhone_go");
		String id_person = request.getParameter("id");
		String id_phone = request.getParameter("phoneId");

		// Добавление записи.
		if (addPerson_go != null) {
			// Создание записи на основе данных из формы.
			Person new_person = new Person(request.getParameter("name"), request.getParameter("surname"),
					request.getParameter("middlename"));
			// Валидация ФИО.
			String error_message = validatePersonFMLName(new_person);
			// Если данные верные, можно производить добавление.
			if (error_message.equals("")) {
				// Если запись удалось добавить...
				if (this.phonebook.addPerson(new_person)) {
					jsp_parameters.put("current_action_result", "ADDITION_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Добавление выполнено успешно");
				} else {
					// Если запись НЕ удалось добавить...
					jsp_parameters.put("current_action_result", "ADDITION_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка добавления");
				}

				// Установка параметров JSP.
				request.setAttribute("jsp_parameters", jsp_parameters);
				// Передача запроса в JSP.
				dispatcher_for_list.forward(request, response);
			} 
			// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
			else {
				// Подготовка параметров для JSP.
    			jsp_parameters.put("current_action", "addPerson");
				jsp_parameters.put("next_action", "addPerson_go");
				jsp_parameters.put("next_action_label", "Добавить");
				jsp_parameters.put("error_message", error_message);

				request.setAttribute("person", new_person);
				request.setAttribute("jsp_parameters", jsp_parameters);
				dispatcher_for_adding_person.forward(request, response);
			}
		}

		if (editPerson_go != null) {
			// Получение записи и её обновление на основе данных из формы.
			Person updatable_person = this.phonebook.getPerson(request.getParameter("id"));
			updatable_person.setName(request.getParameter("name"));
			updatable_person.setSurname(request.getParameter("surname"));
			updatable_person.setMiddlename(request.getParameter("middlename"));

			// Валидация ФИО.
			String error_message = this.validatePersonFMLName(updatable_person); 
			

			// Если данные верные, можно производить добавление.
			if (error_message.equals("")) {
				// Если запись удалось обновить...
				if (this.phonebook.updatePerson(id_person, updatable_person)) {
					jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Обновление выполнено успешно");
				} 
				// Если запись НЕ удалось обновить...
				else {
					jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка обновления");
				}

				// Установка параметров JSP.
				request.setAttribute("jsp_parameters", jsp_parameters);
				// Передача запроса в JSP.
				dispatcher_for_list.forward(request, response);
			} 
			// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
			else {
				// Подготовка параметров для JSP.
    			jsp_parameters.put("current_action", "editPerson");
				jsp_parameters.put("next_action", "editPerson_go");
				jsp_parameters.put("next_action_label", "Сохранить");
				jsp_parameters.put("error_message", error_message);

				// Установка параметров JSP.
    			request.setAttribute("person", updatable_person);
				request.setAttribute("jsp_parameters", jsp_parameters);
				// Передача запроса в JSP.
    			dispatcher_for_manager.forward(request, response);
			}
		}

		if (addPhone_go != null) {
			Person add_phone_person = this.phonebook.getPerson(request.getParameter("id"));
			String phone = request.getParameter("phone");
			String error_message = "";

			if (!Person.validatePhone(phone)) {
				error_message = "Телефон должен содержать от 2 до 50 символов: цифра, +, -, #.";
			}

			if (error_message.equals("")) {
				if (this.phonebook.addPhone(add_phone_person, phone)) {
					jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
					jsp_parameters.put("next_action_label", "Сохранить");
					jsp_parameters.put("next_action", "editPerson_go");
					jsp_parameters.put("current_action_result_label", "Обновление выполнено успешно");
				} else {
					jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка обновления");
				}
				request.setAttribute("jsp_parameters", jsp_parameters);
				request.setAttribute("person", add_phone_person);
				dispatcher_for_manager.forward(request, response);
			} else {
				jsp_parameters.put("current_action", "addPhone");
				jsp_parameters.put("next_action", "addPhone_go");
				jsp_parameters.put("next_action_label", "Добавить номер");
				jsp_parameters.put("error_message", error_message);

				request.setAttribute("person", add_phone_person);
				request.setAttribute("jsp_parameters", jsp_parameters);
				dispatcher_for_adding_phone.forward(request, response);
			}
		}

		if (editPhone_go != null) {
			// Получение записи и её обновление на основе данных из формы.
			Person edit_phone_person = this.phonebook.getPerson(request.getParameter("id"));
			String phone = request.getParameter("phone");
			String error_message = "";

			if (!Person.validatePhone(phone)) {
				error_message = "Телефон должен содержать от 2 до 50 символов: цифра, +, -, #.";
			}

			if (error_message.equals("")) {
				// Если запись удалось обновить...
				if (this.phonebook.editPhone(id_phone, edit_phone_person, phone)) {
					jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
					jsp_parameters.put("next_action_label", "Сохранить");
					jsp_parameters.put("next_action", "editPerson_go");
					jsp_parameters.put("current_action_result_label", "Обновление выполнено успешно");
				} else {
					jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка обновления");
				}
				request.setAttribute("jsp_parameters", jsp_parameters);
				request.setAttribute("person", edit_phone_person);
				dispatcher_for_manager.forward(request, response);
			} else {
				jsp_parameters.put("current_action", "editPhone");
				jsp_parameters.put("next_action", "editPhone_go");
				jsp_parameters.put("next_action_label", "Сохранить номер");
				jsp_parameters.put("error_message", error_message);

				request.setAttribute("person", edit_phone_person);
				request.setAttribute("jsp_parameters", jsp_parameters);
				request.setAttribute("phoneId", id_phone);

				dispatcher_for_editing_phone.forward(request, response);
			}
		}
	}
}
